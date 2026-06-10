/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.internaltestsupport.controllers

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext}
import api.models.errors.{ErrorWrapper, MtdError}
import api.models.outcomes.ResponseWrapper
import api.services.AuthService
import cats.data.EitherT
import config.AppConfig
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.internaltestsupport.models.*
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}
import uk.gov.hmrc.internaltestsupport.services.{GGAutomationService, LookupService, OAuthService}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

//TODO: this controller should be feature switched along with its route so it is never published past QA

@Singleton
class OAuthController @Inject() (
    val cc: ControllerComponents,
    val authService: AuthService,
    ggAuthService: GGAutomationService,
    oauthService: OAuthService,
    idLookup: LookupService,
    idGenerator: IdGenerator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc)
    with Logging {

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val endpointLogContext             = EndpointLogContext(controllerName = "OauthController", endpointName = "GetOauthToken")
    implicit val ctx: RequestContext   = RequestContext.from(idGenerator, endpointLogContext)
    implicit val correlationId: String = idGenerator.getCorrelationId

    import uk.gov.hmrc.internaltestsupport.utils.ErrorWrapperSyntax.*

    request.body
      .validate[SubmitRawRequest]
      .fold(
        errs => Future.successful(BadRequest(JsError.toJson(errs))),
        submission => {

          val clientId     = appConfig.atsAppClientId
          val clientSecret = appConfig.atsAppClientSecret

          def resolvedMtdId: EitherT[Future, ErrorWrapper, MtdIdReference] =
            submission.identifier.fold {
              EitherT(idLookup.getMtdId(submission.nino)).leftMap(_.toErrorWrapper)
            } { id =>
              EitherT.rightT[Future, ErrorWrapper](MtdIdReference(id))
            }

          def exchangeOAuthCode(code: String): EitherT[Future, ErrorWrapper, ResponseWrapper[OAuthResponse]] =
            EitherT(
              oauthService.getOAuthToken(
                OAuthRequest(
                  grant_type = "authorization_code",
                  code = code,
                  redirect_uri = "http://localhost:9000",
                  client_id = clientId,
                  client_secret = clientSecret
                )
              )
            )

          val result =
            for {
              mtdIdReference <- resolvedMtdId
              ggAuthResponse <- EitherT(
                ggAuthService.submitForm(
                  SubmitRequest.from(submission, mtdIdReference.mtdbsa)
                )
              )
              oauthResponse <- exchangeOAuthCode(ggAuthResponse.responseData.oauthCode)
            } yield Ok(Json.toJson(SubmitResponse(s"Bearer ${oauthResponse.responseData.access_token}")))

          result
            .leftMap[Result] { errorWrapper =>
              logger.error(s"${errorWrapper.error.code} error in OAuth flow: ${errorWrapper.error.message}")
              val leftResult = errorResultMap(errorWrapper)
              leftResult
            }
            .merge
        }
      )
  }

  //TODO: check all error cases and return appropriate status codes
  private def errorResultMap(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case GrantScopeRetrievalError | OAuthCodeRetrievalError => UnprocessableEntity(Json.toJson(errorWrapper))
      case PWTimeoutError | PWError                           => InternalServerError(Json.toJson(errorWrapper))
    }
  }

}

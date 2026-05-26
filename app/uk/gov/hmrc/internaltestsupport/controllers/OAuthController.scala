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
import api.models.errors.ErrorWrapper
import api.services.AuthService
import cats.data.EitherT
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.internaltestsupport.models.oauth.OAuthRequest
import uk.gov.hmrc.internaltestsupport.models.*
import uk.gov.hmrc.internaltestsupport.services.{GGAutomationService, OAuthService}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OAuthController @Inject() (
    val cc: ControllerComponents,
    val authService: AuthService,
    ggAuthService: GGAutomationService,
    oauthService: OAuthService,
    idGenerator: IdGenerator
)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "OauthController", endpointName = "GetOauthToken")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.body
      .validate[AuthSubmitRawRequest]
      .fold(
        errs => Future.successful(BadRequest(JsError.toJson(errs))),
        submission => {

          val identifier   = "XXIT00001017604"                      // TODO source from 1171
          val clientId     = "PyWtcKfIuJ729591F3STlG7lSAXN"         // TODO config
          val clientSecret = "3e89acac-7df4-4825-a881-f75e20329495" // TODO config

          val result: EitherT[Future, ErrorWrapper, Result] = {
            for {
              ggAuthResponse <- EitherT(ggAuthService.submitForm(AuthSubmitRequest.from(submission, identifier)))
              oauthCode <- {
                val oauthRequest = OAuthRequest(
                  grant_type = "authorization_code",
                  code = ggAuthResponse.responseData.oauthCode,
                  redirect_uri = "http://localhost:9000",
                  client_id = clientId,
                  client_secret = clientSecret
                )
                EitherT(oauthService.getOAuthToken(oauthRequest))
              }
            } yield {
              Ok(Json.obj("Authorization" -> s"Bearer ${oauthCode.responseData.access_token}"))
            }
          }

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

  private def errorResultMap(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case GrantScopeRetrievalError | OAuthCodeRetrievalError => UnprocessableEntity(Json.toJson(errorWrapper))
      case PWTimeoutError | PWError                           => InternalServerError(Json.toJson(errorWrapper))
    }
  }

}

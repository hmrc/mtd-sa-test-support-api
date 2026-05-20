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
import api.services.AuthService
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.internaltestsupport.models.oauth.OAuthRequest
import uk.gov.hmrc.internaltestsupport.models.{FormSubmitRawRequest, FormSubmitRequest}
import uk.gov.hmrc.internaltestsupport.services.{FormAutomationService, OAuthService}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class OAuthController @Inject() (
    val cc: ControllerComponents,
    val authService: AuthService,
    ggAuthService: FormAutomationService,
    oauthService: OAuthService,
    idGenerator: IdGenerator
)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "OauthController", endpointName = "GetOauthToken")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    // If you want to keep `.as[...]` for now, that’s fine (but validate is safer; see below)
    request.body
      .validate[FormSubmitRawRequest]
      .fold(
        errs => Future.successful(BadRequest(JsError.toJson(errs))),
        submission => {

          val identifier   = "XXIT00001017604"                      // TODO source from 1171
          val clientId     = "PyWtcKfIuJ729591F3STlG7lSAXN"         // TODO config
          val clientSecret = "3e89acac-7df4-4825-a881-f75e20329495" // TODO config

          ggAuthService
            .submitForm(FormSubmitRequest.from(submission, identifier))
            .flatMap { result =>
              logger.info(s"Form submission result: $result")

              result.oauthCode match {
                case None =>
                  // Minimal behaviour for now: message + 404/400 as you prefer
                  val msg = result.error.getOrElse("No oauthCode returned from GG auth journey")
                  logger.warn(msg)
                  Future.successful(NotFound(Json.obj("error" -> msg)))

                case Some(code) =>
                  logger.info(s"OAuth code received: $code")

                  val oauthRequest = OAuthRequest(
                    grant_type = "authorization_code",
                    code = code,
                    redirect_uri = "http://localhost:9000",
                    client_id = clientId,
                    client_secret = clientSecret
                  )

                  oauthService.getOAuthToken(oauthRequest).map {
                    case Right(response) =>
                      Ok(Json.obj("Authorization" -> s"Bearer ${response.responseData.access_token}"))
                    case Left(error) =>
                      logger.error(s"Error fetching OAuth token: $error")
                      InternalServerError(Json.obj("error" -> error.toString))
                  }
              }
            }
            .recover { case NonFatal(e) =>
              logger.error("Unexpected failure in OAuth flow", e)
              InternalServerError(Json.obj("error" -> e.getMessage))
            }
        }
      )
  }

}

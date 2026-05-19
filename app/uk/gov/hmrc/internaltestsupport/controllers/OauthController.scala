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

import api.controllers.AuthorisedController
import api.services.AuthService
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.internaltestsupport.models.{FormSubmitRawRequest, FormSubmitRequest}
import uk.gov.hmrc.internaltestsupport.services.FormAutomationService
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class OauthController @Inject() (val cc: ControllerComponents, val authService: AuthService, ggAuthService: FormAutomationService)(implicit
    ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    // parse this
    val submission = request.body.as[FormSubmitRawRequest]
    val identifier = "XXIT00001017604"

    ggAuthService.submitForm(FormSubmitRequest.from(submission, identifier)).map { result =>
      logger.info(s"Form submission result: $result")
      Ok(Json.obj("oauth_code" -> result.oauthCode))
    }

  }

}

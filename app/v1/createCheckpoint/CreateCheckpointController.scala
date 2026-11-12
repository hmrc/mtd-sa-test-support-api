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

package v1.createCheckpoint

import api.controllers.*
import api.services.AuthService
import api.utils.{IdGenerator, Logging}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateCheckpointController @Inject() (val cc: ControllerComponents,
                                            val authService: AuthService,
                                            validatorFactory: CreateCheckpointValidatorFactory,
                                            service: CreateCheckpointService,
                                            idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def handleRequest(nino: Option[String]): Action[AnyContent] = authorisedAction().async { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "CreateCheckpointController", endpointName = "CreateCheckpoint")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.headers.get("X-Client-Id") match {
      case Some(vendorClientId) =>
        val validator = validatorFactory.validator(vendorClientId, nino)

        val requestHandler = RequestHandler
          .withValidator(validator)
          .withService(service.createCheckpoint)
          .withPlainJsonResult(CREATED)

        requestHandler.handleRequest()
      case None =>
        logger.warn("[CreateCheckpointController] [CreateCheckpoint] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }
  }

}

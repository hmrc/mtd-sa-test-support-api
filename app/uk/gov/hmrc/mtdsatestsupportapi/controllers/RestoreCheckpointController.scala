/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mtdsatestsupportapi.controllers

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.services.AuthService
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.RestoreCheckpointRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.models.request.restoreCheckpoint.RestoreCheckpointRawData
import uk.gov.hmrc.mtdsatestsupportapi.services.RestoreCheckpointService
import utils.{IdGenerator, Logging}

import javax.inject.{Inject, Singleton}
import scala.concurrent.*

@Singleton
class RestoreCheckpointController @Inject() (cc: ControllerComponents,
                                             val authService: AuthService,
                                             parser: RestoreCheckpointRequestParser,
                                             service: RestoreCheckpointService,
                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def handleRequest(checkpointId: String): Action[AnyContent] = authorisedAction().async { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "RestoreCheckpointController", endpointName = "RestoreCheckpoint")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)
    request.headers.get("X-Client-Id") match {
      case Some(vendorClientId) =>
        val rawData = RestoreCheckpointRawData(vendorClientId, checkpointId)

        val requestHandler = RequestHandler
          .withParser(parser)
          .withService(service.restoreCheckpoint)
          .withNoContentResult(CREATED)
        requestHandler.handleRequest(rawData)

      case None =>
        logger.warn("[RestoreCheckpointController] [RestoreCheckpoint] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }
  }

}

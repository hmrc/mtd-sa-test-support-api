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

import api.controllers.*
import api.services.AuthService
import play.api.mvc.*
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.ListCheckpointsRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRawData
import uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints.*
import uk.gov.hmrc.mtdsatestsupportapi.services.ListCheckpointsService
import utils.{IdGenerator, Logging}

import javax.inject.{Inject, Singleton}
import scala.concurrent.*

@Singleton
class ListCheckpointsController @Inject() (val authService: AuthService,
                                           cc: ControllerComponents,
                                           parser: ListCheckpointsRequestParser,
                                           service: ListCheckpointsService,
                                           idGenerator: IdGenerator
                                          )(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def handleRequest(nino: Option[String]): Action[AnyContent] = authorisedAction().async { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "ListCheckpointsController", endpointName = "ListCheckpoints")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.headers.get("X-Client-Id") match {
      case Some(vendorClientId) =>
        val rawData = ListCheckpointsRawData(vendorClientId, nino)

        val requestHandler = RequestHandler
          .withParser(parser)
          .withService(service.listCheckpoints)
          .withPlainJsonResult()
        requestHandler.handleRequest(rawData)

      case None =>
        logger.warn("[ListCheckpointsController] [ListCheckpoints] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }
  }

}

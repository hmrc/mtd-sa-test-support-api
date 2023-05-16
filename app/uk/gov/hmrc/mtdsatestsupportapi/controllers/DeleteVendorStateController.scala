/*
 * Copyright 2023 HM Revenue & Customs
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

import api.controllers._
import api.services.EnrolmentsAuthService
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.DeleteStatefulTestDataRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRawData
import uk.gov.hmrc.mtdsatestsupportapi.services.DeleteVendorStateService
import utils.{IdGenerator, Logging}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteVendorStateController @Inject() (val cc: ControllerComponents,
                                             val authService: EnrolmentsAuthService,
                                             parser: DeleteStatefulTestDataRequestParser,
                                             service: DeleteVendorStateService,
                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def handleRequest(): Action[AnyContent] = authorisedAction().async { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "DeleteVendorStateController", endpointName = "DeleteStatefulTestData")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.headers.get("X-Client-Id") match {
      case Some(vendorClientId) =>
        val rawData = DeleteStatefulTestDataRawData(vendorClientId, None)

        val requestHandler = RequestHandler
          .withParser(parser)
          .withService(service.deleteVendorState)

        requestHandler.handleRequest(rawData)

      case None =>
        logger.warn("[DeleteVendorStateController] [DeleteStatefulTestData] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }

  }

}

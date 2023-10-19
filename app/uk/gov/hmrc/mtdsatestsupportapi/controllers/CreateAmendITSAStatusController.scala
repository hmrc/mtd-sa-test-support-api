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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.hateoas.HateoasFactory
import api.services.AuthService
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.CreateAmendITSAStatusRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRawData
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createAmendITSAStatus.CreateAmendITSAStatusHateoasData
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createAmendITSAStatus.CreateAmendITSAStatusResponse.LinksFactory
import uk.gov.hmrc.mtdsatestsupportapi.services.CreateAmendITSAStatusService
import utils.{IdGenerator, Logging}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendITSAStatusController @Inject() (val cc: ControllerComponents,
                                                 val authService: AuthService,
                                                 parser: CreateAmendITSAStatusRequestParser,
                                                 service: CreateAmendITSAStatusService,
                                                 hateoasFactory: HateoasFactory,
                                                 idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc)
    with Logging {

  def handleRequest(nino: String, taxYear: String): Action[JsValue] = authorisedAction().async(parse.json) { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "CreateAmendITSAStatusController", endpointName = "CreateAmendITSAStatus")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.headers.get("X-Client-Id") match {
      case Some(_) =>
        val rawData = CreateAmendITSAStatusRawData( nino, taxYear, request.body)
        val requestHandler = RequestHandler
          .withParser(parser)
          .withService(service.createAmend)
          .withHateoasResultFrom(hateoasFactory)(
            (request, _) => CreateAmendITSAStatusHateoasData(request.nino, request.taxYear),
            NO_CONTENT
          )

        requestHandler.handleRequest(rawData)

      case None =>
        logger.warn("[CreateAmendITSAStatusController] [CreateAmendITSAStatus] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }
  }
}

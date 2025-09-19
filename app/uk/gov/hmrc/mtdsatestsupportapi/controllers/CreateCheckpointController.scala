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

import api.controllers.*
import api.hateoas.HateoasFactory
import api.services.AuthService
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.CreateCheckpointRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRawData
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.CreateCheckpointHateoasData
import uk.gov.hmrc.mtdsatestsupportapi.services.CreateCheckpointService
import utils.{IdGenerator, Logging}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateCheckpointController @Inject() (val cc: ControllerComponents,
                                            val authService: AuthService,
                                            parser: CreateCheckpointRequestParser,
                                            service: CreateCheckpointService,
                                            hateoasFactory: HateoasFactory,
                                            idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  def handleRequest(nino: Option[String]): Action[AnyContent] = authorisedAction().async { implicit request =>
    val endpointLogContext           = EndpointLogContext(controllerName = "CreateCheckpointController", endpointName = "CreateCheckpoint")
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    request.headers.get("X-Client-Id") match {
      case Some(vendorClientId) =>
        val rawData = CreateCheckpointRawData(vendorClientId, nino)

        val requestHandler = RequestHandler
          .withParser(parser)
          .withService(service.createCheckpoint)
          .withHateoasResultFrom(hateoasFactory)(
            (request, response) => CreateCheckpointHateoasData(request.nino, response.checkpointId),
            CREATED
          )

        requestHandler.handleRequest(rawData)

      case None =>
        logger.warn("[CreateCheckpointController] [CreateCheckpoint] - No X-Client-Id header present in the request")
        Future.successful(InternalServerError)
    }
  }

}

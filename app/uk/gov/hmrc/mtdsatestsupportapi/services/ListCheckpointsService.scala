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

package uk.gov.hmrc.mtdsatestsupportapi.services

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.implicits.toBifunctorOps
import uk.gov.hmrc.mtdsatestsupportapi.connectors.ListCheckpointsConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints.{Checkpoint, ListCheckpointsResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ListCheckpointsService @Inject() (connector: ListCheckpointsConnector) extends BaseService {

  def listCheckpoints(request: ListCheckpointsRequest)(implicit
      ec: ExecutionContext,
      rc: RequestContext): Future[Either[ErrorWrapper, ResponseWrapper[ListCheckpointsResponse[Checkpoint]]]] =
    connector.listCheckpoints(request).map(_.leftMap(mapDownstreamErrors(stubErrorMap)))

  private val stubErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "NOT_FOUND"                 -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

}

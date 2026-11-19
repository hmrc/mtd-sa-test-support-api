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

import api.controllers.RequestContext
import api.models.errors.{MtdError, NotFoundError}
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v1.createCheckpoint.model.request.CreateCheckpointRequestData
import v1.createCheckpoint.model.response.CreateCheckpointResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateCheckpointService @Inject() (connector: CreateCheckpointConnector) extends BaseService {

  def createCheckpoint(
      request: CreateCheckpointRequestData)(implicit ec: ExecutionContext, rc: RequestContext): Future[ServiceOutcome[CreateCheckpointResponse]] =
    connector.createCheckpoint(request).map(_.leftMap(mapDownstreamErrors(stubErrorMap)))

  private val stubErrorMap: Map[String, MtdError] = Map("NOT_FOUND" -> NotFoundError)

}

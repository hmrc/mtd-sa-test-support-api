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

package v1.restoreCheckpoint

import api.controllers.RequestContext
import api.models.errors.{MtdError, NotFoundError}
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v1.restoreCheckpoint.model.request.RestoreCheckpointRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestoreCheckpointService @Inject() (connector: RestoreCheckpointConnector) extends BaseService {

  def restoreCheckpoint(request: RestoreCheckpointRequestData)(implicit
      ec: ExecutionContext,
      rc: RequestContext
  ): Future[ServiceOutcome[Unit]] =
    connector.restoreCheckpoint(request).map(_.leftMap(mapDownstreamErrors(stubErrorMap)))

  private val stubErrorMap: Map[String, MtdError] = Map("NOT_FOUND" -> NotFoundError)

}

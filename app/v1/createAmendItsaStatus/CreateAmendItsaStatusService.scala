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

package v1.createAmendItsaStatus

import api.controllers.RequestContext
import api.models.errors.{InternalError, MtdError}
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v1.createAmendItsaStatus.model.request.CreateAmendItsaStatusRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendItsaStatusService @Inject() (connector: CreateAmendItsaStatusConnector) extends BaseService {

  def createAmendItsaStatus(
      request: CreateAmendItsaStatusRequestData)(implicit ec: ExecutionContext, rc: RequestContext): Future[ServiceOutcome[Unit]] =
    connector.createAmendItsaStatus(request).map(_.leftMap(mapDownstreamErrors(stubErrorMap)))

  private val stubErrorMap: Map[String, MtdError] = Map(
    "SERVER_ERROR"        -> InternalError,
    "SERVICE_UNAVAILABLE" -> InternalError
  )

}

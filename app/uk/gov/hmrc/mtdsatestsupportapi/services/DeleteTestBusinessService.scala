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
import api.models.errors.{ErrorWrapper, MtdError, NotFoundError}
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.implicits.toBifunctorOps
import uk.gov.hmrc.mtdsatestsupportapi.connectors.DeleteTestBusinessConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.DeleteTestBusinessRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteTestBusinessService @Inject() (connector: DeleteTestBusinessConnector) extends BaseService {

  def deleteTestBusiness(
      request: DeleteTestBusinessRequest)(implicit ec: ExecutionContext, rc: RequestContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] =
    connector.deleteTestBusiness(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = Map("NOT_FOUND" -> NotFoundError)
}

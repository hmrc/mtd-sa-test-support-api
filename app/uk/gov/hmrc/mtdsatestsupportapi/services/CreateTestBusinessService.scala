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
import cats.implicits._
import uk.gov.hmrc.mtdsatestsupportapi.connectors.CreateTestBusinessConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.CreateTestBusinessRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.CreateTestBusiness.CreateTestBusinessResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateTestBusinessService @Inject()(connector: CreateTestBusinessConnector) extends BaseService {

  def createTestBusiness(request: CreateTestBusinessRequest)(implicit
                                                         ec: ExecutionContext,
                                                         rc: RequestContext): Future[Either[ErrorWrapper, ResponseWrapper[CreateTestBusinessResponse]]] =
    connector.createTestBusiness(request).map(_.leftMap(mapDownstreamErrors(stubErrorMap)))

  private val stubErrorMap: Map[String, MtdError] =
    Map(
      "DUPLICATE_PROPERTY_BUSINESS" -> RulePropertyBusinessAddedError
    )

}
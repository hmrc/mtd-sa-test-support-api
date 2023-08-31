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

import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, ErrorWrapper, NotFoundError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockDeleteTestBusinessConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.DeleteTestBusinessRequest

import scala.concurrent.Future

class DeleteTestBusinessServiceSpec extends ServiceSpec with MockDeleteTestBusinessConnector {

  private val service       = new DeleteTestBusinessService(mockDeleteTestBusinessConnector)
  private val correlationId = "X-123"
  private val requestData   = DeleteTestBusinessRequest( Nino("AA999999A"), businessId = "XAIS12345678910")

  "DeleteTestBusinessService" when {
    "the connector call is successful" must {
      "return the success result" in {
        MockDeleteTestBusinessConnector.deleteTestBusiness(requestData).returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.deleteTestBusiness(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
    "the connector call is unsuccessful" must {
      "map the received downstream errors to mtd errors" in {
        MockDeleteTestBusinessConnector
          .deleteTestBusiness(requestData)
          .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("NOT_FOUND"))))))

        await(service.deleteTestBusiness(requestData)) shouldBe Left(ErrorWrapper(correlationId, NotFoundError))
      }
    }
  }

}

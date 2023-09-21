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
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import org.scalactic.source.Position
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateTestBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockCreateTestBusinessConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.CreateTestBusinessRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.CreateTestBusiness.CreateTestBusinessResponse

import scala.concurrent.Future

class CreateTestBusinessServiceSpec extends ServiceSpec with MockCreateTestBusinessConnector with CreateTestBusinessFixtures {
  import MinimalCreateTestBusinessRequest.SelfEmployment._

  private val service = new CreateTestBusinessService(mockCreateTestBusinessConnector)

  private val request  = CreateTestBusinessRequest(nino = Nino("TC663795B"), business)
  private val response = CreateTestBusinessResponse("someBusinessId")

  "CreateTestBusinessService" when {

    "the service call is successful" should {
      "return the result" in {
        MockCreateTestBusinessConnector
          .CreateTestBusiness(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createTestBusiness(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "the service call is unsuccessful" should {
      "map the received errors from the stub" when {
        def serviceErrorTest(stubErrorCode: String, mtdError: MtdError)
                            (implicit pos: Position): Unit =
          s"a $stubErrorCode error is returned from the service" in {

            MockCreateTestBusinessConnector
              .CreateTestBusiness(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            await(service.createTestBusiness(request)) shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        serviceErrorTest("DUPLICATE_PROPERTY_BUSINESS", RulePropertyBusinessAddedError)
      }

    }
  }

}

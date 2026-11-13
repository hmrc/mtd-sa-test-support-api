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

package v1.createTestBusiness

import api.connectors.DownstreamOutcome
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.{maxSelfEmploymentRequestBodyModel, responseBodyModel}
import v1.createTestBusiness.model.request.CreateTestBusinessRequestData
import v1.createTestBusiness.model.response.CreateTestBusinessResponse

import scala.concurrent.Future

class CreateTestBusinessServiceSpec extends ServiceSpec {

  private val requestData: CreateTestBusinessRequestData = CreateTestBusinessRequestData(
    nino = Nino("AA123456A"),
    body = maxSelfEmploymentRequestBodyModel
  )

  "CreateTestBusinessService" when {
    "the connector call is successful" should {
      "return the success result" in new Test {
        val outcome: DownstreamOutcome[CreateTestBusinessResponse] = Right(ResponseWrapper(correlationId, responseBodyModel))

        MockCreateTestBusinessConnector
          .createTestBusiness(requestData)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[CreateTestBusinessResponse] = await(service.createTestBusiness(requestData))

        result shouldBe outcome
      }
    }

    "the connector call is unsuccessful" should {
      "map errors according to spec" when {
        "a DUPLICATE_PROPERTY_BUSINESS error is returned from the service" in new Test {
          MockCreateTestBusinessConnector
            .createTestBusiness(requestData)
            .returns(
              Future.successful(
                Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("DUPLICATE_PROPERTY_BUSINESS"))))
              )
            )

          val result: ServiceOutcome[CreateTestBusinessResponse] = await(service.createTestBusiness(requestData))

          result shouldBe Left(ErrorWrapper(correlationId, RulePropertyBusinessAddedError))
        }
      }
    }
  }

  private trait Test extends MockCreateTestBusinessConnector {
    val service: CreateTestBusinessService = new CreateTestBusinessService(mockCreateTestBusinessConnector)
  }

}

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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.{maxSelfEmploymentRequestBodyModel, responseBodyModel}
import v1.createTestBusiness.model.request.CreateTestBusinessRequestData
import v1.createTestBusiness.model.response.CreateTestBusinessResponse

import scala.concurrent.Future

class CreateTestBusinessConnectorSpec extends ConnectorSpec {

  private val nino: Nino = Nino("AA123456A")

  private val requestData: CreateTestBusinessRequestData = CreateTestBusinessRequestData(
    nino = nino,
    body = maxSelfEmploymentRequestBodyModel
  )

  "CreateTestBusinessConnector" when {
    "receiving a downstream 201 success response" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[CreateTestBusinessResponse] = Right(ResponseWrapper(correlationId, responseBodyModel))

        willPost(
          url = url"$baseUrl/test-support/business-details/$nino",
          body = maxSelfEmploymentRequestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateTestBusinessResponse] = await(connector.createTestBusiness(requestData))

        result shouldBe outcome
      }
    }

    "receiving a downstream error response" should {
      "return a unsuccessful response" in new Test {
        val downstreamErrorResponse: DownstreamErrors              = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: DownstreamOutcome[CreateTestBusinessResponse] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPost(
          url = url"$baseUrl/test-support/business-details/$nino",
          body = maxSelfEmploymentRequestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateTestBusinessResponse] = await(connector.createTestBusiness(requestData))

        result shouldBe outcome
      }
    }
  }

  private trait Test extends StubTest {
    protected val connector: CreateTestBusinessConnector = new CreateTestBusinessConnector(mockHttpClient, mockAppConfig)
  }

}

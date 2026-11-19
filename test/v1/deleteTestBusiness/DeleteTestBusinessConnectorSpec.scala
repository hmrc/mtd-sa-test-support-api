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

package v1.deleteTestBusiness

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.deleteTestBusiness.model.request.DeleteTestBusinessRequestData

import scala.concurrent.Future

class DeleteTestBusinessConnectorSpec extends ConnectorSpec {

  private val nino: String       = "AA123456A"
  private val businessId: String = "X0IS12345678901"

  private val requestData: DeleteTestBusinessRequestData = DeleteTestBusinessRequestData(
    nino = Nino(nino),
    businessId = BusinessId(businessId)
  )

  "DeleteTestBusinessConnector" when {
    "receiving a downstream 204 success response" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/test-support/business-details/$nino/$businessId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteTestBusiness(requestData))

        result shouldBe outcome
      }
    }

    "receiving a downstream error response" should {
      "return a unsuccessful response" in new Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: DownstreamOutcome[Unit]          = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willDelete(
          url = url"$baseUrl/test-support/business-details/$nino/$businessId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteTestBusiness(requestData))

        result shouldBe outcome
      }
    }
  }

  private trait Test extends StubTest {
    protected val connector: DeleteTestBusinessConnector = new DeleteTestBusinessConnector(mockHttpClient, mockAppConfig)
  }

}

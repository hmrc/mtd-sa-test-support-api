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

package v1.deleteStatefulTestData

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.deleteStatefulTestData.model.request.DeleteStatefulTestDataRequestData

import scala.concurrent.Future

class DeleteStatefulTestDataConnectorSpec extends ConnectorSpec {

  private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
  private val nino: String           = "AA123456A"

  private def requestData(nino: Option[Nino] = None): DeleteStatefulTestDataRequestData = DeleteStatefulTestDataRequestData(
    vendorClientId = vendorClientId,
    nino = nino
  )

  "DeleteStatefulTestDataConnector" when {
    "a valid request with query parameter nino receives a downstream 204 success response" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/test-support/vendor-state/$vendorClientId",
          parameters = Seq("taxableEntityId" -> nino)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteStatefulTestData(requestData(Some(Nino(nino)))))

        result shouldBe outcome
      }
    }

    "a valid request without query parameter nino receives a downstream 204 success response" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/test-support/vendor-state/$vendorClientId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteStatefulTestData(requestData()))

        result shouldBe outcome
      }
    }

    "receiving a downstream error response" should {
      "return a unsuccessful response" in new Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: DownstreamOutcome[Unit]          = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willDelete(
          url = url"$baseUrl/test-support/vendor-state/$vendorClientId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteStatefulTestData(requestData()))

        result shouldBe outcome
      }
    }
  }

  private trait Test extends StubTest {
    protected val connector: DeleteStatefulTestDataConnector = new DeleteStatefulTestDataConnector(mockHttpClient, mockAppConfig)
  }

}

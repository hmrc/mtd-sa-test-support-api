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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.requestBodyModel
import v1.createAmendItsaStatus.model.request.*

import scala.concurrent.Future

class CreateAmendItsaStatusConnectorSpec extends ConnectorSpec {

  private val nino: Nino       = Nino("AA123456A")
  private val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

  private val requestData: CreateAmendItsaStatusRequestData = CreateAmendItsaStatusRequestData(
    nino = nino,
    taxYear = taxYear,
    body = requestBodyModel
  )

  "CreateAmendItsaStatusConnector" when {
    "receiving a downstream 204 success response" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        willPost(
          url = url"$baseUrl/test-support/itsa-details/$nino/${taxYear.asTysDownstream}",
          body = requestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendItsaStatus(requestData))

        result shouldBe outcome
      }
    }

    "receiving a downstream error response" should {
      "return a unsuccessful response" in new Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: DownstreamOutcome[Unit]          = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPost(
          url = url"$baseUrl/test-support/itsa-details/$nino/${taxYear.asTysDownstream}",
          body = requestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendItsaStatus(requestData))

        result shouldBe outcome
      }
    }
  }

  private trait Test extends StubTest {
    protected val connector: CreateAmendItsaStatusConnector = new CreateAmendItsaStatusConnector(mockHttpClient, mockAppConfig)
  }

}

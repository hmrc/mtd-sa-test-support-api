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

package uk.gov.hmrc.mtdsatestsupportapi.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{
  CreateAmendITSAStatusRequest,
  CreateAmendITSAStatusRequestBody,
  ITSAStatusDetail
}

class CreateAmendITSAStatusConnectorSpec extends ConnectorSpec {

  private val nino             = Nino("TC663795B")
  private val taxYear          = TaxYear.fromMtd("2022-23")
  private val itsaStatusDetail = ITSAStatusDetail("2019-01-01T00:00:00.000Z", Status.`01`, StatusReason.`01`, None)
  private val body             = CreateAmendITSAStatusRequestBody(List(itsaStatusDetail))

  private val request = CreateAmendITSAStatusRequest(nino, taxYear, body)

  private trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateAmendITSAStatusConnector(httpClientV2, mockAppConfig)
  }

  "CreateAmendITSAStatusConnector" when {
    "the downstream returns a successful 204 response" should {
      "return a successful response" in new StubTest with Test {
        when(POST, s"/test-support/itsa-details/$nino/${taxYear.asTys}")
          .withRequestBody(body)
          .withHeaders(requiredHeaders)
          .thenReturnNoContent(headers = responseHeaders)

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))
        result shouldBe Right(ResponseWrapper(responseCorrelationId, ()))
      }
    }

    "the downstream response is an error" must {
      "return a failure result" in new StubTest with Test {
        when(POST, s"/test-support/itsa-details/$nino/${taxYear.asTys}")
          .withHeaders(requiredHeaders)
          .thenReturn(400, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), responseHeaders)

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))
        result shouldBe Left(ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }

}

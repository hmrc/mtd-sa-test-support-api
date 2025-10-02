/*
 * Copyright 2025 HM Revenue & Customs
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

import api.connectors.ConnectorSpec
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRequest

class DeleteVendorStateConnectorSpec extends ConnectorSpec {
  private val vendorId        = "someVendor"
  private val nino            = "AA123456A"
  private val request         = DeleteStatefulTestDataRequest(vendorId, None)
  private val requestWithNino = DeleteStatefulTestDataRequest(vendorId, Some(Nino(nino)))

  trait Test {
    self: ConnectorTest =>

    protected val connector = new DeleteVendorStateConnector(httpClientV2, mockAppConfig)
  }

  "deleteVendorState" when {
    "the downstream returns a successful response" must {
      "return a successful result" in new StubTest with Test {
        when(DELETE, s"/test-support/vendor-state/$vendorId")
          .withHeaders(requiredHeaders)
          .thenReturnNoContent(headers = responseHeaders)

        await(connector.deleteVendorState(request)) shouldBe Right(ResponseWrapper(responseCorrelationId, ()))
      }

      "return a successful result with nino" in new StubTest with Test {
        when(DELETE, s"/test-support/vendor-state/$vendorId")
          .withQueryParams(Seq("taxableEntityId" -> nino))
          .withHeaders(requiredHeaders)
          .thenReturnNoContent(headers = responseHeaders)

        await(connector.deleteVendorState(requestWithNino)) shouldBe Right(ResponseWrapper(responseCorrelationId, ()))
      }
    }

    "the downstream response is an error" must {
      "return a failure result" in new StubTest with Test {
        when(DELETE, s"/test-support/vendor-state/$vendorId")
          .withHeaders(requiredHeaders)
          .thenReturn(400, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), responseHeaders)

        await(connector.deleteVendorState(request)) shouldBe Left(
          ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }

}

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

import api.connectors.ConnectorSpec
import api.models.domain.{CheckpointId, Nino}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.CREATED
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.CreateCheckpointResponse

class CreateCheckpointConnectorSpec extends ConnectorSpec {
  private val vendorId = "someVendor"
  private val nino     = "AA123456A"
  private val request  = CreateCheckpointRequest(vendorId, Nino(nino))

  trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateCheckpointConnector(httpClientV2, mockAppConfig)
  }

  "CreateCheckpoint connector" when {
    "the downstream returns a successful 201 response" must {
      "return a successful result with the checkpointId" in new StubTest with Test {
        val checkpointId: CheckpointId = CheckpointId("someCheckpointId")

        when(POST, s"/test-support/vendor-state/$vendorId/checkpoints")
          .withQueryParams(Seq("taxableEntityId" -> nino))
          .withHeaders(requiredHeaders)
          .thenReturn[JsValue](CREATED, headers = responseHeaders, body = Json.obj("checkpointId" -> checkpointId))

        await(connector.createCheckpoint(request)) shouldBe Right(ResponseWrapper(responseCorrelationId, CreateCheckpointResponse(checkpointId)))
      }
    }

    "the downstream response is an error" must {
      "return a failure result" in new StubTest with Test {
        when(POST, s"/test-support/vendor-state/$vendorId/checkpoints")
          .withQueryParams(Seq("taxableEntityId" -> nino))
          .withHeaders(requiredHeaders)
          .thenReturn(400, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), responseHeaders)

        await(connector.createCheckpoint(request)) shouldBe Left(
          ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }

}

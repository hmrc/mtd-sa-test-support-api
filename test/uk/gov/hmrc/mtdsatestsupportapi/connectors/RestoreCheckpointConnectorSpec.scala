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
import api.models.domain.CheckpointId
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.mtdsatestsupportapi.models.request.restoreCheckpoint.RestoreCheckpointRequest

class RestoreCheckpointConnectorSpec extends ConnectorSpec {

  "RestoreCheckpointConnector" when {
    "receiving a downstream 201 success response" must {
      "return a success response" in new StubTest with Test {
        when(method = POST, uri = downstreamUri)
          .withHeaders(requiredHeaders)
          .thenReturnNoContent(headers = responseHeaders)

        await(connector.restoreCheckpoint(requestData)) shouldBe Right(ResponseWrapper(responseCorrelationId, ()))
      }
    }
    "receiving a downstream error response" must {
      "return a unsuccessful response" in new StubTest with Test {
        when(method = POST, uri = downstreamUri)
          .withHeaders(requiredHeaders)
          .thenReturn(BAD_REQUEST, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), responseHeaders)

        await(connector.restoreCheckpoint(requestData)) shouldBe Left(
          ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val vendorClientId = "someVendorId"
    protected val checkpointId   = "someCheckpointId"
    protected val downstreamUri  = s"/test-support/vendor-state/$vendorClientId/checkpoints/$checkpointId/restore"

    protected val requestData: RestoreCheckpointRequest = RestoreCheckpointRequest(vendorClientId, CheckpointId("someCheckpointId"))

    protected val connector = new RestoreCheckpointConnector(mockAppConfig, httpClientV2)
  }

}

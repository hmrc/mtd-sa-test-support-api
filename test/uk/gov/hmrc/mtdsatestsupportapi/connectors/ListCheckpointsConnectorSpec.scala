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
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints.{Checkpoint, ListCheckpointsResponse}

class ListCheckpointsConnectorSpec extends ConnectorSpec {

  "ListCheckpointsConnector" when {
    "the downstream returns a successful 200 response" when {
      "nino is present" should {
        "return the response data" in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId")
            .withQueryParams(Seq("taxableEntityId" -> nino))
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](status = 200, body = responseWithNino, headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithNino)) shouldBe Right(
            ResponseWrapper(responseCorrelationId, ListCheckpointsResponse(Seq(Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp)))))
        }
      }
      "no nino is present" should {
        "return the response data" in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId")
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](status = 200, body = responseWithoutNino, headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithoutNino)) shouldBe Right(
            ResponseWrapper(responseCorrelationId, ListCheckpointsResponse(Seq(Checkpoint(checkpointId, None, checkpointCreationTimestamp)))))
        }
      }
      "the downstream call is unsuccessful" should {
        "return the corresponding errors " in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId")
            .withQueryParams(Seq("taxableEntityId" -> nino))
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](404, body = Json.obj("code" -> "SOME_NOT_FOUND_ERROR", "reason" -> "Some message"), headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithNino)) shouldBe Left(
            ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_NOT_FOUND_ERROR"))))
        }
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector = new ListCheckpointsConnector(mockAppConfig, httpClientV2)

    protected val vendorId                    = "some_client_id"
    protected val nino                        = "AA123456A"
    protected val checkpointId                = "some_checkpoint_id"
    protected val checkpointCreationTimestamp = "2019-01-01T00:00:00.000Z"

    protected val requestDataWithNino: ListCheckpointsRequest    = ListCheckpointsRequest(vendorId, Some(Nino(nino)))
    protected val requestDataWithoutNino: ListCheckpointsRequest = ListCheckpointsRequest(vendorId, None)

    protected val responseWithNino: JsObject = Json.obj(
      "checkpoints" -> Json.arr(
        Json.obj("checkpointId" -> checkpointId, "nino" -> nino, "checkpointCreationTimestamp" -> checkpointCreationTimestamp)))

    protected val responseWithoutNino: JsObject =
      Json.obj("checkpoints" -> Json.arr(Json.obj("checkpointId" -> checkpointId, "checkpointCreationTimestamp" -> checkpointCreationTimestamp)))

  }

}

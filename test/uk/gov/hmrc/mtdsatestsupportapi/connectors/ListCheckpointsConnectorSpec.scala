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
      "querying by nino" should {
        "return the response data associated with the vendorId and nino" in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId/checkpoints")
            .withQueryParams(Seq("taxableEntityId" -> nino))
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](status = 200, body = responseFromNinoQuery, headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithNino)) shouldBe Right(
            ResponseWrapper(responseCorrelationId, ListCheckpointsResponse(Seq(Checkpoint(checkpointId1, Some(nino), checkpointCreationTimestamp1)))))
        }
      }
      "no nino query parameter is present" should {
        "return the response data associated with the vendorId" in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId/checkpoints")
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](status = 200, body = response, headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithoutNino)) shouldBe Right(
            ResponseWrapper(responseCorrelationId, ListCheckpointsResponse(Seq(
              Checkpoint(checkpointId1, Some(nino), checkpointCreationTimestamp1),
              Checkpoint(checkpointId2, None, checkpointCreationTimestamp2)
            ))))
        }
      }
      "the downstream call is unsuccessful" should {
        "return the corresponding errors " in new StubTest with Test {
          when(GET, s"/test-support/vendor-state/$vendorId/checkpoints")
            .withQueryParams(Seq("taxableEntityId" -> nino))
            .withHeaders(requiredHeaders)
            .thenReturn[JsValue](404, body = Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), headers = responseHeaders)

          await(connector.listCheckpoints(requestDataWithNino)) shouldBe Left(
            ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
        }
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector = new ListCheckpointsConnector(mockAppConfig, httpClientV2)

    protected val vendorId                     = "some_client_id"
    protected val nino                         = "AA123456A"
    protected val checkpointId1                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
    protected val checkpointId2                = "b2e4050e-fbbc-47a8-d5b4-65d9f015c253"
    protected val checkpointCreationTimestamp1 = "2019-01-01T00:00:00.000Z"
    protected val checkpointCreationTimestamp2 = "2019-01-02T00:00:00.000Z"

    protected val requestDataWithNino: ListCheckpointsRequest    = ListCheckpointsRequest(vendorId, Some(Nino(nino)))
    protected val requestDataWithoutNino: ListCheckpointsRequest = ListCheckpointsRequest(vendorId, None)

    protected val responseFromNinoQuery: JsObject = Json.obj(
      "checkpoints" -> Json.arr(
        Json.obj("checkpointId" -> checkpointId1, "taxableEntityId" -> nino, "checkpointCreationTimestamp" -> checkpointCreationTimestamp1)))

    protected val response: JsObject =
      Json.obj(
        "checkpoints" -> Json.arr(
          Json.obj("checkpointId" -> checkpointId1, "taxableEntityId" -> nino, "checkpointCreationTimestamp" -> checkpointCreationTimestamp1),
          Json.obj("checkpointId" -> checkpointId2, "checkpointCreationTimestamp" -> checkpointCreationTimestamp2)
        ))

  }

}

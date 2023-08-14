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

package uk.gov.hmrc.mtdsatestsupportapi.services

import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockListCheckpointsConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints.{Checkpoint, ListCheckpointsResponse}

import scala.concurrent.Future

class ListCheckpointsServiceSpec extends ServiceSpec with MockListCheckpointsConnector {

  private val nino                        = "AA123456A"
  private val checkpointId                = "some_checkpoint_id"
  private val checkpointCreationTimestamp = "2019-01-01T00:00:00.000Z"

  private val service = new ListCheckpointsService(mockListCheckpointsConnector)

  private val request  = ListCheckpointsRequest(vendorClientId = "someVendorId", nino = Some(Nino("TC663795B")))
  private val response = ListCheckpointsResponse(Seq(Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp)))

  "ListCheckpointsService" when {
    val correlationId = "X-123"
    "the connector call is successful" should {
      "return the result" in {
        MockListCheckpointsConnector
          .listCheckpoints(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listCheckpoints(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

}

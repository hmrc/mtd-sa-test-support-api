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

package v1.listCheckpoints

import api.connectors.DownstreamOutcome
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.listCheckpoints.fixture.ListCheckpointsFixture.responseBodyModel
import v1.listCheckpoints.model.request.ListCheckpointsRequestData
import v1.listCheckpoints.model.response.ListCheckpointsResponse

import scala.concurrent.Future

class ListCheckpointsServiceSpec extends ServiceSpec {

  private val requestData: ListCheckpointsRequestData = ListCheckpointsRequestData(
    vendorClientId = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7",
    nino = Some(Nino("AA123456A"))
  )

  "ListCheckpointsService" when {
    "the connector call is successful" should {
      "return the success result" in new Test {
        val outcome: DownstreamOutcome[ListCheckpointsResponse] = Right(
          ResponseWrapper(correlationId, responseBodyModel)
        )

        MockListCheckpointsConnector
          .listCheckpoints(requestData)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[ListCheckpointsResponse] = await(service.listCheckpoints(requestData))

        result shouldBe outcome
      }
    }

    "the connector call is unsuccessful" should {
      "map errors according to spec" when {
        "an unmapped error is returned from the service" in new Test {
          MockListCheckpointsConnector
            .listCheckpoints(requestData)
            .returns(
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNMAPPED")))))
            )

          val result: ServiceOutcome[ListCheckpointsResponse] = await(service.listCheckpoints(requestData))

          result shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }
    }
  }

  private trait Test extends MockListCheckpointsConnector {
    val service: ListCheckpointsService = new ListCheckpointsService(mockListCheckpointsConnector)
  }

}

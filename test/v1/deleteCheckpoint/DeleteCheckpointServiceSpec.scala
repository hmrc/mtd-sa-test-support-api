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

package v1.deleteCheckpoint

import api.connectors.DownstreamOutcome
import api.models.domain.CheckpointId
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.deleteCheckpoint.model.request.DeleteCheckpointRequestData

import scala.concurrent.Future

class DeleteCheckpointServiceSpec extends ServiceSpec {

  private val requestData: DeleteCheckpointRequestData = DeleteCheckpointRequestData(
    vendorClientId = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7",
    checkpointId = CheckpointId("a1e8057e-fbbc-47a8-a8b4-78d9f015c253")
  )

  "DeleteCheckpointService" when {
    "the connector call is successful" should {
      "return the success result" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        MockDeleteCheckpointConnector
          .deleteCheckpoint(requestData)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.deleteCheckpoint(requestData))

        result shouldBe outcome
      }
    }

    "the connector call is unsuccessful" should {
      "map errors according to spec" when {
        "a NOT_FOUND error is returned from the service" in new Test {
          MockDeleteCheckpointConnector
            .deleteCheckpoint(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("NOT_FOUND"))))))

          val result: ServiceOutcome[Unit] = await(service.deleteCheckpoint(requestData))

          result shouldBe Left(ErrorWrapper(correlationId, NotFoundError))
        }
      }
    }
  }

  private trait Test extends MockDeleteCheckpointConnector {
    val service: DeleteCheckpointService = new DeleteCheckpointService(mockDeleteCheckpointConnector)
  }

}

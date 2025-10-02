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

import api.models.domain.CheckpointId
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockRestoreCheckpointConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.restoreCheckpoint.RestoreCheckpointRequest

import scala.concurrent.Future

class RestoreCheckpointServiceSpec extends ServiceSpec with MockRestoreCheckpointConnector {

  private val service       = new RestoreCheckpointService(mockRestoreCheckpointConnector)
  private val requestData   = RestoreCheckpointRequest("someVendorId", CheckpointId("someCheckpointId"))

  "RestoreCheckpointService" when {
    "the connector call is successful" should {
      "return the success result" in {
        MockRestoreCheckpointConnector
          .restoreCheckpoint(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.restoreCheckpoint(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "the connector call is unsuccessful" should {
      "map the received downstream errors to mtd errors" when {
        def serviceError(downstreamErrorCode: String, expectedMtdError: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the connector" in {

            MockRestoreCheckpointConnector
              .restoreCheckpoint(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.restoreCheckpoint(requestData)) shouldBe Left(ErrorWrapper(correlationId, expectedMtdError))
          }

        val stubErrors: List[(String, MtdError)] = List("NOT_FOUND" -> NotFoundError)

        stubErrors.foreach { case (downstreamCode, mtdError) =>
          serviceError(downstreamCode, mtdError)
        }
      }

    }
  }

}

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
import api.models.errors._
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

    "the connector call is unsuccessful" should {
      "map the received errors from the stub to mtd errors" when {
        def serviceError(stubErrorCode: String, mtdError: MtdError): Unit =
          s"a $stubErrorCode error is returned from the connector" in {

            MockListCheckpointsConnector
              .listCheckpoints(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            await(service.listCheckpoints(request)) shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        val stubErrors: List[(String, MtdError)] = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "NOT_FOUND"                 -> NotFoundError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        stubErrors.foreach { case (stubErrorCode, mtdError) =>
          serviceError(stubErrorCode, mtdError)
        }
      }

    }
  }

}

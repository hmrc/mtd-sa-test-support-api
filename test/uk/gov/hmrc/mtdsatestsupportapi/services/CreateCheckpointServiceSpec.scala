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

import api.models.domain.{CheckpointId, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockCreateCheckpointConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.CreateCheckpointResponse

import scala.concurrent.Future

class CreateCheckpointServiceSpec extends ServiceSpec with MockCreateCheckpointConnector {

  private val service = new CreateCheckpointService(mockCreateCheckpointConnector)

  private val request  = CreateCheckpointRequest(vendorClientId = "someVendorId", nino = Nino("TC663795B"))
  private val response = CreateCheckpointResponse(CheckpointId("someCheckpointId"))

  "CreateCheckpointService" when {
    val correlationId = "X-123"
    "the service call is successful" should {
      "return the result" in {
        MockCreateCheckpointConnector
          .createCheckpoint(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createCheckpoint(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "the service call is unsuccessful" should {
      "map the received errors from the stub" when {
        def serviceError(stubErrorCode: String, mtdError: MtdError): Unit =
          s"a $stubErrorCode error is returned from the service" in {

            MockCreateCheckpointConnector
              .createCheckpoint(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            await(service.createCheckpoint(request)) shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        val stubErrors: List[(String, MtdError)] = List(
          "NOT_FOUND" -> NotFoundError
        )

        stubErrors.foreach((serviceError _).tupled)
      }

    }
  }

}

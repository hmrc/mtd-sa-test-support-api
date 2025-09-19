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
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockDeleteVendorStateConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRequest

import scala.concurrent.Future

class DeleteVendorStateServiceSpec extends ServiceSpec {

  "DeleteVendorStateService" when {
    val correlationId = "X-123"
    "the service call is successful" should {
      "return the result" in new Test {
        MockDeleteVendorStateConnector
          .deleteVendorState(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.deleteVendorState(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
    "the service call is unsuccessful" should {
      "map the received errors from the stub" when {
        def serviceError(stubErrorCode: String, mtdError: MtdError): Unit =
          s"a $stubErrorCode error is returned from the service" in new Test {

            MockDeleteVendorStateConnector
              .deleteVendorState(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            await(service.deleteVendorState(request)) shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        val stubErrors: List[(String, MtdError)] = List(
          "NOT_FOUND" -> NotFoundError,
          "SERVER_ERROR" -> InternalError,
          "SERVICE_UNAVAILABLE" -> InternalError
        )

        stubErrors.foreach(elem => serviceError(elem._1, elem._2))
      }

    }
  }

  trait Test extends MockDeleteVendorStateConnector {
    protected val service = new DeleteVendorStateService(mockDeleteVendorStateConnector)

    protected val request: DeleteStatefulTestDataRequest = DeleteStatefulTestDataRequest(vendorClientId = "someId", nino = Some(Nino("TC663795B")))
  }
}

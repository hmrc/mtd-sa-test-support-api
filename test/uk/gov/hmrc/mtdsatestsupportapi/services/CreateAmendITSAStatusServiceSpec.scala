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

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockCreateAmendITSAStatusConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{
  CreateAmendITSAStatusRequest,
  CreateAmendITSAStatusRequestBody,
  ITSAStatusDetail
}

import scala.concurrent.Future

class CreateAmendITSAStatusServiceSpec extends ServiceSpec with MockCreateAmendITSAStatusConnector {

  private val nino             = Nino("TC663795B")
  private val taxYear          = TaxYear.fromMtd("2022-23")
  private val itsaStatusDetail = ITSAStatusDetail("2019-01-01T00:00:00.000Z", Status.`No Status`, StatusReason.`Sign up - return available`, None)
  private val body             = CreateAmendITSAStatusRequestBody(List(itsaStatusDetail))

  private val request = CreateAmendITSAStatusRequest(nino, taxYear, body)

  private val service = new CreateAmendITSAStatusService(mockCreateAmendITSAStatusConnector)

  "CreateAmendITSAStatusService" when {
    "the service call is successful" should {
      "return the result" in {
        MockedCreateAmendITSAStatusConnector
          .createAmend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result = await(service.createAmend(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "the service call is unsuccessful" should {
      "map the received errors from the stub" when {
        def serviceError(stubErrorCode: String, mtdError: MtdError): Unit =
          s"a $stubErrorCode error is returned from the service" in {
            MockedCreateAmendITSAStatusConnector
              .createAmend(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            val result = await(service.createAmend(request))
            result shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        val stubErrors = List(
          "SERVER_ERROR"        -> InternalError,
          "SERVICE_UNAVAILABLE" -> InternalError
        )

        stubErrors.foreach((serviceError _).tupled)
      }

    }
  }

}

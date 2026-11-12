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

package v1.createAmendItsaStatus

import api.connectors.DownstreamOutcome
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.requestBodyModel
import v1.createAmendItsaStatus.model.request.*

import scala.concurrent.Future

class CreateAmendItsaStatusServiceSpec extends ServiceSpec {

  private val requestData: CreateAmendItsaStatusRequestData = CreateAmendItsaStatusRequestData(
    nino = Nino("AA123456A"),
    taxYear = TaxYear.fromMtd("2023-24"),
    body = requestBodyModel
  )

  "CreateAmendItsaStatusService" when {
    "the connector call is successful" should {
      "return the success result" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendItsaStatusConnector
          .createAmendItsaStatus(requestData)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.createAmendItsaStatus(requestData))

        result shouldBe outcome
      }
    }

    "the connector call is unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            MockCreateAmendItsaStatusConnector
              .createAmendItsaStatus(requestData)
              .returns(
                Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode)))))
              )

            val result: ServiceOutcome[Unit] = await(service.createAmendItsaStatus(requestData))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap: Seq[(String, MtdError)] = List(
          "SERVER_ERROR"        -> InternalError,
          "SERVICE_UNAVAILABLE" -> InternalError
        )

        errorMap.foreach(serviceError.tupled)
      }
    }
  }

  private trait Test extends MockCreateAmendItsaStatusConnector {
    val service: CreateAmendItsaStatusService = new CreateAmendItsaStatusService(mockCreateAmendItsaStatusConnector)
  }

}

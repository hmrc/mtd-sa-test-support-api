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
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors.MockCreateBusinessConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness.CreateBusinessRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createBusiness.CreateBusinessResponse

import scala.concurrent.Future

class CreateBusinessServiceSpec extends ServiceSpec with MockCreateBusinessConnector with CreateBusinessFixtures {
  import MinimalCreateBusinessRequest._

  private val service = new CreateBusinessService(mockCreateBusinessConnector)

  private val request  = CreateBusinessRequest(nino = Nino("TC663795B"), business)
  private val response = CreateBusinessResponse("someBusinessId")

  "CreateBusinessService" when {

    "the service call is successful" should {
      "return the result" in {
        MockCreateBusinessConnector
          .createBusiness(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createBusiness(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "the service call is unsuccessful" should {
      "map the received errors from the stub" when {
        def serviceError(stubErrorCode: String, mtdError: MtdError): Unit =
          s"a $stubErrorCode error is returned from the service" in {

            MockCreateBusinessConnector
              .createBusiness(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(stubErrorCode))))))

            await(service.createBusiness(request)) shouldBe Left(ErrorWrapper(correlationId, mtdError))
          }

        val stubErrors: List[(String, MtdError)] = List(
          "DUPLICATE_PROPERTY_BUSINESS" -> RulePropertyBusinessAddedError
        )

        stubErrors.foreach((serviceError _).tupled)
      }

    }
  }

}

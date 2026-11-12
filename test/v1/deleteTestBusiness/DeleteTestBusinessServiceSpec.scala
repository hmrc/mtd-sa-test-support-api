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

package v1.deleteTestBusiness

import api.connectors.DownstreamOutcome
import api.models.domain.{BusinessId, Nino}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.deleteTestBusiness.model.request.DeleteTestBusinessRequestData

import scala.concurrent.Future

class DeleteTestBusinessServiceSpec extends ServiceSpec {

  private val requestData: DeleteTestBusinessRequestData = DeleteTestBusinessRequestData(
    nino = Nino("AA123456A"),
    businessId = BusinessId("X0IS12345678901")
  )

  "DeleteTestBusinessService" when {
    "the connector call is successful" should {
      "return the success result" in new Test {
        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        MockDeleteTestBusinessConnector
          .deleteTestBusiness(requestData)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.deleteTestBusiness(requestData))

        result shouldBe outcome
      }
    }

    "the connector call is unsuccessful" should {
      "map errors according to spec" when {
        "a NOT_FOUND error is returned from the service" in new Test {
          MockDeleteTestBusinessConnector
            .deleteTestBusiness(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("NOT_FOUND"))))))

          val result: ServiceOutcome[Unit] = await(service.deleteTestBusiness(requestData))

          result shouldBe Left(ErrorWrapper(correlationId, NotFoundError))
        }
      }
    }
  }

  private trait Test extends MockDeleteTestBusinessConnector {
    val service: DeleteTestBusinessService = new DeleteTestBusinessService(mockDeleteTestBusinessConnector)
  }

}

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

package v1.createCheckpoint

import api.controllers.ControllerTestRunner
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import v1.createCheckpoint.fixture.CreateCheckpointFixture.*
import v1.createCheckpoint.model.request.CreateCheckpointRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateCheckpointControllerSpec extends ControllerTestRunner with MockCreateCheckpointService with MockCreateCheckpointValidatorFactory {

  private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"

  private val requestData: CreateCheckpointRequestData = CreateCheckpointRequestData(
    vendorClientId = vendorClientId,
    nino = Nino(nino)
  )

  private val requestWithClientId: FakeRequest[AnyContentAsEmpty.type] = fakePostEmptyRequest.withHeaders("X-Client-Id" -> vendorClientId)

  "CreateCheckpointController" when {
    "X-Client-Id header is present" should {
      "return 201 CREATED" when {
        "the request is valid" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateCheckpointService
            .createCheckpoint(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

          runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(responseBodyJson))
        }
      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test() {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(expectedError = NinoFormatError)
        }

        "the service returns an error" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateCheckpointService
            .createCheckpoint(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(expectedError = NotFoundError)
        }
      }
    }

    "X-Client-Id header is not present" should {
      "return 500 INTERNAL_SERVER_ERROR" in new Test(fakePostEmptyRequest) {
        runStatusCheck(expectedStatus = InternalError.httpStatus)
      }
    }
  }

  private class Test(request: FakeRequest[AnyContentAsEmpty.type] = requestWithClientId) extends ControllerTest {

    private val controller: CreateCheckpointController = new CreateCheckpointController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      validatorFactory = mockCreateCheckpointValidatorFactory,
      service = mockCreateCheckpointService,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(Some(nino))(request)
  }

}

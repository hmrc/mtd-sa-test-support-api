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

package v1.restoreCheckpoint

import api.controllers.ControllerTestRunner
import api.models.domain.CheckpointId
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import v1.restoreCheckpoint.model.request.RestoreCheckpointRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RestoreCheckpointControllerSpec extends ControllerTestRunner with MockRestoreCheckpointService with MockRestoreCheckpointValidatorFactory {

  private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
  private val checkpointId: String   = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val requestData: RestoreCheckpointRequestData = RestoreCheckpointRequestData(
    vendorClientId = vendorClientId,
    checkpointId = CheckpointId(checkpointId)
  )

  private val requestWithClientId: FakeRequest[AnyContentAsEmpty.type] = fakePostEmptyRequest.withHeaders("X-Client-Id" -> vendorClientId)

  "RestoreCheckpointController" when {
    "X-Client-Id header is present" should {
      "return 201 CREATED" when {
        "the request is valid" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockRestoreCheckpointService
            .restoreCheckpoint(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          runOkTest(expectedStatus = CREATED)
        }
      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test() {
          willUseValidator(returning(CheckpointIdFormatError))

          runErrorTest(expectedError = CheckpointIdFormatError)
        }

        "the service returns an error" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockRestoreCheckpointService
            .restoreCheckpoint(requestData)
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

    private val controller: RestoreCheckpointController = new RestoreCheckpointController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      validatorFactory = mockRestoreCheckpointValidatorFactory,
      service = mockRestoreCheckpointService,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(checkpointId)(request)
  }

}

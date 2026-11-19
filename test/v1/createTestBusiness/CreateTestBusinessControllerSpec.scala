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

package v1.createTestBusiness

import api.controllers.ControllerTestRunner
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.*
import v1.createTestBusiness.model.request.CreateTestBusinessRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateTestBusinessControllerSpec extends ControllerTestRunner with MockCreateTestBusinessService with MockCreateTestBusinessValidatorFactory {

  private val requestData: CreateTestBusinessRequestData = CreateTestBusinessRequestData(
    nino = Nino(nino),
    body = maxSelfEmploymentRequestBodyModel
  )

  private val requestWithClientId: FakeRequest[JsValue] =
    fakePostRequest(mtdSelfEmploymentRequestBodyJson).withHeaders("X-Client-Id" -> "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7")

  "CreateTestBusinessController" when {
    "X-Client-Id header is present" should {
      "return 201 CREATED" when {
        "the request is valid" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateTestBusinessService
            .createTestBusiness(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

          runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(mtdResponseBodyJson))
        }
      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test() {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(expectedError = NinoFormatError)
        }

        "the service returns an error" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateTestBusinessService
            .createTestBusiness(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RulePropertyBusinessAddedError))))

          runErrorTest(expectedError = RulePropertyBusinessAddedError)
        }
      }
    }

    "X-Client-Id header is not present" should {
      "return 500 INTERNAL_SERVER_ERROR" in new Test(fakePostRequest(mtdSelfEmploymentRequestBodyJson)) {
        runStatusCheck(expectedStatus = InternalError.httpStatus)
      }
    }
  }

  private class Test(request: FakeRequest[JsValue] = requestWithClientId) extends ControllerTest {

    private val controller: CreateTestBusinessController = new CreateTestBusinessController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      validatorFactory = mockCreateTestBusinessValidatorFactory,
      service = mockCreateTestBusinessService,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(request)
  }

}

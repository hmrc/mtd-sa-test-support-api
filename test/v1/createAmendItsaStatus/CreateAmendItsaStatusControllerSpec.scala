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

import api.controllers.ControllerTestRunner
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.*
import v1.createAmendItsaStatus.model.request.CreateAmendItsaStatusRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendItsaStatusControllerSpec
    extends ControllerTestRunner
    with MockCreateAmendItsaStatusService
    with MockCreateAmendItsaStatusValidatorFactory {

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2023-24"

  private val requestData: CreateAmendItsaStatusRequestData = CreateAmendItsaStatusRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestBodyModel
  )

  private val requestWithClientId: FakeRequest[JsValue] =
    fakePostRequest(mtdRequestBodyJson).withHeaders("X-Client-Id" -> "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7")

  "CreateAmendItsaStatusController" when {
    "X-Client-Id header is present" should {
      "return 204 NO_CONTENT" when {
        "the request is valid" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateAmendItsaStatusService
            .createAmendItsaStatus(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          runOkTest(expectedStatus = NO_CONTENT)
        }
      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test() {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(expectedError = NinoFormatError)
        }

        "the service returns an error" in new Test() {
          willUseValidator(returningSuccess(requestData))

          MockCreateAmendItsaStatusService
            .createAmendItsaStatus(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

          runErrorTest(expectedError = InternalError)
        }
      }
    }

    "X-Client-Id header is not present" should {
      "return 500 INTERNAL_SERVER_ERROR" in new Test(fakePostRequest(mtdRequestBodyJson)) {
        runStatusCheck(expectedStatus = InternalError.httpStatus)
      }
    }
  }

  private class Test(request: FakeRequest[JsValue] = requestWithClientId) extends ControllerTest {

    private val controller: CreateAmendItsaStatusController = new CreateAmendItsaStatusController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      validatorFactory = mockCreateAmendItsaStatusValidatorFactory,
      service = mockCreateAmendItsaStatusService,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(request)
  }

}

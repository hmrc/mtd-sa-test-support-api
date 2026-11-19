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

package v1.deleteStatefulTestData

import api.controllers.ControllerTestRunner
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import v1.deleteStatefulTestData.model.request.DeleteStatefulTestDataRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteStatefulTestDataControllerSpec
    extends ControllerTestRunner
    with MockDeleteStatefulTestDataService
    with MockDeleteStatefulTestDataValidatorFactory {

  private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"

  private def requestData(nino: Option[Nino] = None): DeleteStatefulTestDataRequestData = DeleteStatefulTestDataRequestData(
    vendorClientId = vendorClientId,
    nino = nino
  )

  private val requestWithClientId: FakeRequest[AnyContentAsEmpty.type] = fakeDeleteRequest.withHeaders("X-Client-Id" -> vendorClientId)

  "DeleteStatefulTestDataController" when {
    "X-Client-Id header is present" should {
      "return 204 NO_CONTENT" when {
        "the request is valid with nino query parameter" in new Test(nino = Some(nino)) {
          willUseValidator(returningSuccess(requestData(Some(Nino(nino)))))

          MockDeleteStatefulTestDataService
            .deleteStatefulTestData(requestData(Some(Nino(nino))))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          runOkTest(expectedStatus = NO_CONTENT)
        }

        "the request is valid without nino query parameter" in new Test() {
          willUseValidator(returningSuccess(requestData()))

          MockDeleteStatefulTestDataService
            .deleteStatefulTestData(requestData())
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
          willUseValidator(returningSuccess(requestData()))

          MockDeleteStatefulTestDataService
            .deleteStatefulTestData(requestData())
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(expectedError = NotFoundError)
        }
      }
    }

    "X-Client-Id header is not present" should {
      "return 500 INTERNAL_SERVER_ERROR" in new Test(fakeDeleteRequest) {
        runStatusCheck(expectedStatus = InternalError.httpStatus)
      }
    }
  }

  private class Test(request: FakeRequest[AnyContentAsEmpty.type] = requestWithClientId, nino: Option[String] = None) extends ControllerTest {

    private val controller: DeleteStatefulTestDataController = new DeleteStatefulTestDataController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      validatorFactory = mockDeleteStatefulTestDataValidatorFactory,
      service = mockDeleteStatefulTestDataService,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(request)
  }

}

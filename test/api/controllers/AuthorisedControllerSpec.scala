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

package api.controllers

import api.mocks.services.MockAuthService
import api.models.errors.{ClientNotAuthenticatedError, ClientNotAuthorisedError, InternalError, InvalidBearerTokenError, MtdError}
import api.services.AuthService
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedControllerSpec extends ControllerBaseSpec {

  trait Test extends MockAuthService {
    val hc: HeaderCarrier = HeaderCarrier()

    class TestController extends AuthorisedController(cc) {
      override val authService: AuthService = mockEnrolmentsAuthService

      def action(): Action[AnyContent] = authorisedAction().async {
        Future.successful(Ok(Json.obj()))
      }

    }

    lazy val target = new TestController()
  }

  val predicate: Predicate = Enrolment("HMRC-MTD-IT") or Enrolment("HMRC-AS-AGENT")

  "calling an action" when {

    "the user is authorised" should {
      "return a 200" in new Test {
        MockedEnrolmentsAuthService.authoriseUser()

        private val result = target.action()(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the MTD user is not authorised" when {

      def testNotAuth(error: MtdError): Unit =
        s"the service returns an error with code ${error.code}" must {
          s"return a ${error.httpStatus} with code ${error.code}" in new Test {
            MockedEnrolmentsAuthService
              .authorised
              .returns(Future.successful(Left(error)))

            private val result = target.action()(fakeGetRequest)
            contentAsJson(result) shouldBe error.asJson
            status(result) shouldBe error.httpStatus
          }
        }

      Seq(ClientNotAuthenticatedError, ClientNotAuthorisedError, InvalidBearerTokenError)
        .foreach(testNotAuth)
    }

    "auth returns an unexpected error" should {
      "return a 500" in new Test {
        MockedEnrolmentsAuthService
          .authorised
          .returns(Future.successful(Left(InternalError)))

        private val result = target.action()(fakeGetRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

}

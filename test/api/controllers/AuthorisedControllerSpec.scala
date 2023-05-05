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

import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.errors._
import api.models.errors.{InvalidBearerTokenError, ClientNotAuthenticatedError, InternalError}
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedControllerSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService with MockMtdIdLookupService {
    val hc: HeaderCarrier = HeaderCarrier()

    class TestController extends AuthorisedController(cc) {
      override val authService: EnrolmentsAuthService = mockEnrolmentsAuthService
      override val lookupService: MtdIdLookupService  = mockMtdIdLookupService

      def action(nino: String): Action[AnyContent] = authorisedAction(nino).async {
        Future.successful(Ok(Json.obj()))
      }

    }

    lazy val target = new TestController()
  }

  val nino  = "AA123456A"
  val mtdId = "X123567890"

  val predicate: Predicate = Enrolment("HMRC-MTD-IT")
    .withIdentifier("MTDITID", mtdId)
    .withDelegatedAuthRule("mtd-it-auth")

  "calling an action" when {

    "the user is authorised" should {
      "return a 200" in new Test {

        MockMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Right(mtdId)))

        MockedEnrolmentsAuthService.authoriseUser()

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "auth returns an unexpected error" should {
      "return a 500" in new Test {

        MockMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Right(mtdId)))

        MockedEnrolmentsAuthService
          .authorised(predicate)
          .returns(Future.successful(Left(InternalError)))

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the nino is invalid" should {
      "return a 400" in new Test {

        MockMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Left(NinoFormatError)))

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the nino is valid but invalid bearer token" should {
      "return a 401" in new Test {

        MockMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Left(InvalidBearerTokenError)))

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe UNAUTHORIZED
      }
    }

  }

  "authorisation checks fail when retrieving the MDT ID" should {
    "return a 403" in new Test {

      MockMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Left(ClientNotAuthorisedError)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

  "the an error occurs retrieving the MDT ID" should {
    "return a 500" in new Test {

      MockMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Left(InternalError)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "the MTD user is not authenticated" should {
    "return a 401" in new Test {

      MockMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Right(mtdId)))

      MockedEnrolmentsAuthService
        .authorised(predicate)
        .returns(Future.successful(Left(ClientNotAuthenticatedError)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

  "the MTD user is not authorised" should {
    "return a 403" in new Test {

      MockMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Right(mtdId)))

      MockedEnrolmentsAuthService
        .authorised(predicate)
        .returns(Future.successful(Left(ClientNotAuthenticatedError)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

}

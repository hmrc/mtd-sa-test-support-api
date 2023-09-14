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

package api.services

import api.models.auth.UserDetails
import api.models.errors.{ClientNotAuthenticatedError, InvalidBearerTokenError, MtdError}
import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import play.api.Configuration
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceSpec extends ServiceSpec with MockAppConfig {

  "calling .authorised" when {
    "allow authorised users" in new Test {
      MockedAuthConnector
        .authorised()
        .returns(Future.unit)

      await(target.authorised) shouldBe Right(UserDetails("Authorised", None))
    }

    "disallow users that are not logged in" in
      authFailure(InvalidBearerToken(), InvalidBearerTokenError)

    "disallow requests without a bearer token" in
      authFailure(MissingBearerToken(), InvalidBearerTokenError)

    "disallow users where auth fails in other ways" in
      authFailure(new AuthorisationException("general auth failure") {}, ClientNotAuthenticatedError)

    def authFailure(authException: AuthorisationException, expectedError: MtdError): Unit = new Test {
      MockedAuthConnector
        .authorised()
        .returns(Future.failed(authException))

      await(target.authorised) shouldBe Left(expectedError)
    }

    "calling auth is bypassed" must {
      "do nothing (but return a successful result anyway)" in new BaseTest(callAuthEnabled = false) {
        await(target.authorised) shouldBe Right(UserDetails("Authorised", None))
      }
    }
  }

  class Test extends BaseTest(callAuthEnabled = true)

  abstract class BaseTest(callAuthEnabled: Boolean) {
    MockAppConfig.featureSwitches.returns(Configuration("callAuth.enabled" -> callAuthEnabled)).anyNumberOfTimes()

    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    lazy val target                      = new AuthService(mockAuthConnector, mockAppConfig)

    protected val emptyPredicate: EmptyPredicate.type = EmptyPredicate
    private val emptyRetrieval                        = EmptyRetrieval

    object MockedAuthConnector {

      def authorised(): CallHandler[Future[Unit]] = {
        (mockAuthConnector
          .authorise(_: Predicate, _: Retrieval[Unit])(_: HeaderCarrier, _: ExecutionContext))
          .expects(emptyPredicate, emptyRetrieval, *, *)
      }

    }

  }

}

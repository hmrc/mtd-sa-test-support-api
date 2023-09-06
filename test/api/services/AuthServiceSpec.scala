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
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceSpec extends ServiceSpec with MockAppConfig {

  "calling .authorised" when {
    "allow authorised users" in new Test {
      // As all affinity groups are handled the same way
      val someAffinityGroup: AffinityGroup = AffinityGroup.Agent

      MockedAuthConnector
        .authorised(emptyPredicate, affinityGroup)
        .returns(Future.successful(Some(someAffinityGroup)))

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
        .authorised(emptyPredicate, affinityGroup)
        .returns(Future.failed(authException))

      await(target.authorised) shouldBe Left(expectedError)
    }
  }

  trait Test {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    lazy val target                      = new AuthService(mockAuthConnector, mockAppConfig)

    protected val emptyPredicate: EmptyPredicate.type = EmptyPredicate

    object MockedAuthConnector {

      def authorised[A](predicate: Predicate, retrievals: Retrieval[A]): CallHandler[Future[A]] = {
        (mockAuthConnector
          .authorise[A](_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
          .expects(predicate, retrievals, *, *)
      }

    }

  }

}

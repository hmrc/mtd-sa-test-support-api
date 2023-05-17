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
import api.models.errors.{ClientNotAuthenticatedError, ClientNotAuthorisedError, InternalError, InvalidBearerTokenError, MtdError}
import api.models.outcomes.AuthOutcome
import config.ConfidenceLevelConfig
import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.retrieve.~

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsAuthServiceSpec extends ServiceSpec with MockAppConfig {

  private def agentEnrolments(identifier: EnrolmentIdentifier) = Enrolments(Set(Enrolment("HMRC-AS-AGENT", Seq(identifier), "Active")))

  private val ignoredEnrolments = Enrolments(Set())

  "calling .authorised" when {

    "confidence level checks are on" should {
      behave like new AuthBehaviours(authValidationEnabled = true)
    }

    "confidence level checks are off" should {
      behave like new AuthBehaviours(authValidationEnabled = false)
    }

    class AuthBehaviours(authValidationEnabled: Boolean) {
      val expectedPredicate: Predicate =
        if (authValidationEnabled)
          Enrolment("HMRC-MTD-IT") or Enrolment("HMRC-AS-AGENT") and
            ((AffinityGroup.Individual and ConfidenceLevel.L200) or AffinityGroup.Organisation or AffinityGroup.Agent)
        else
          Enrolment("HMRC-MTD-IT") or Enrolment("HMRC-AS-AGENT")

      "allow authorised individuals" in authSuccess(AffinityGroup.Individual, "Individual")
      "allow authorised organisations" in authSuccess(AffinityGroup.Organisation, "Organisation")

      "when an agent has no ARN identifier" must {
        val enrolmentsWithoutArn: Enrolments = agentEnrolments(EnrolmentIdentifier("SomeOtherIdentifier", "123567890"))

        "disallow the agent" in authAgent(enrolmentsWithoutArn, Left(InternalError))
      }

      "when an agent has an ARN identifier" must {
        val arn                           = "123567890"
        val enrolmentsWithArn: Enrolments = agentEnrolments(EnrolmentIdentifier("AgentReferenceNumber", arn))

        "allow the agent" in authAgent(enrolmentsWithArn, Right(UserDetails("Agent", Some(arn))))
      }

      "disallow users without enrolments" in
        authFailure(InsufficientEnrolments(), ClientNotAuthorisedError)

      "disallow users that are not logged in" in
        authFailure(InvalidBearerToken(), InvalidBearerTokenError)

      "disallow requests without a bearer token" in
        authFailure(MissingBearerToken(), InvalidBearerTokenError)

      "disallow users where auth fails in other ways" in
        authFailure(new AuthorisationException("general auth failure") {}, ClientNotAuthenticatedError)

      def authSuccess(userAffinityGroup: AffinityGroup, userTypeName: String): Unit =
        new Test {
          mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

          MockedAuthConnector
            .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
            .returns(Future.successful(new ~(Some(userAffinityGroup), ignoredEnrolments)))

          await(target.authorised) shouldBe Right(UserDetails(userTypeName, None))
        }

      def authFailure(authException: AuthorisationException, expectedError: MtdError): Unit = new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.failed(authException))

        await(target.authorised) shouldBe Left(expectedError)
      }

      def authAgent(enrolments: Enrolments, expectedResult: AuthOutcome): Unit = new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.successful(new ~(Some(AffinityGroup.Agent), enrolments)))

        await(target.authorised) shouldBe expectedResult
      }
    }
  }

  trait Test {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    lazy val target                      = new EnrolmentsAuthService(mockAuthConnector, mockAppConfig)

    object MockedAuthConnector {

      def authorised[A](predicate: Predicate, retrievals: Retrieval[A]): CallHandler[Future[A]] = {
        (mockAuthConnector
          .authorise[A](_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
          .expects(predicate, retrievals, *, *)
      }

    }

    def mockConfidenceLevelCheckConfig(authValidationEnabled: Boolean): Unit = {
      MockAppConfig.confidenceLevelCheckEnabled.returns(
        ConfidenceLevelConfig(
          confidenceLevel = ConfidenceLevel.L200,
          definitionEnabled = true,
          authValidationEnabled = authValidationEnabled
        )
      )
    }

  }

}

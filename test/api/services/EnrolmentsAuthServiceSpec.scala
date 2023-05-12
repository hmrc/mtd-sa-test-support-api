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
import api.models.errors.{ClientNotAuthenticatedError, InternalError}
import config.ConfidenceLevelConfig
import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsAuthServiceSpec extends ServiceSpec with MockAppConfig {

  private def extraPredicatesAnd(predicate: Predicate) = predicate and
    ((AffinityGroup.Individual and ConfidenceLevel.L200) or AffinityGroup.Organisation or AffinityGroup.Agent)

  "calling .authorised" when {
    val inputPredicate = EmptyPredicate

    "confidence level checks are on" should {
      behave like authService(authValidationEnabled = true, extraPredicatesAnd(inputPredicate))
    }

    "confidence level checks are off" should {
      behave like authService(authValidationEnabled = false, inputPredicate)
    }

    def authService(authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit = {
      behave like authorisedIndividual(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like authorisedOrganisation(inputPredicate, authValidationEnabled, expectedPredicate)

      behave like authorisedAgentsMissingArn(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like authorisedAgents(inputPredicate, authValidationEnabled, expectedPredicate)

      behave like disallowUsersWithoutEnrolments(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like disallowWhenNotLoggedIn(inputPredicate, authValidationEnabled, expectedPredicate)
    }

    def authorisedIndividual(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised individuals" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.successful(Some(AffinityGroup.Individual)))

        await(target.authorised(inputPredicate)) shouldBe Right(UserDetails("Individual", None))
      }

    def authorisedOrganisation(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised organisations" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.successful(Some(AffinityGroup.Organisation)))

        await(target.authorised(inputPredicate)) shouldBe Right(UserDetails("Organisation", None))
      }

    def authorisedAgentsMissingArn(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit = {
      "disallow agents that are missing an ARN" in new Test {
        val enrolmentsWithoutArn: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-AS-AGENT",
              Seq(EnrolmentIdentifier("SomeOtherIdentifier", "123567890")),
              "Active"
            )
          )
        )

        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.successful(Some(AffinityGroup.Agent)))

        MockedAuthConnector
          .authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"), authorisedEnrolments)
          .returns(Future.successful(enrolmentsWithoutArn))

        await(target.authorised(inputPredicate)) shouldBe Left(InternalError)
      }
    }

    def authorisedAgents(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised agents with ARN" in new Test {
        val arn = "123567890"
        val enrolmentsWithArn: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-AS-AGENT",
              Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
              "Active"
            )
          )
        )

        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.successful(Some(AffinityGroup.Agent)))

        MockedAuthConnector
          .authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"), authorisedEnrolments)
          .returns(Future.successful(enrolmentsWithArn))

        await(target.authorised(inputPredicate)) shouldBe Right(UserDetails("Agent", Some(arn)))

      }

    def disallowWhenNotLoggedIn(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "disallow users that are not logged in" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.failed(MissingBearerToken()))

        await(target.authorised(inputPredicate)) shouldBe Left(ClientNotAuthenticatedError)
      }

    def disallowUsersWithoutEnrolments(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "disallow users without enrolments" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup)
          .returns(Future.failed(InsufficientEnrolments()))

        await(target.authorised(inputPredicate)) shouldBe Left(ClientNotAuthenticatedError)
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

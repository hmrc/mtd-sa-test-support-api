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
import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import api.models.errors.{InternalError, ClientNotAuthenticatedError}
import api.models.outcomes.AuthOutcome
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject() (val connector: AuthConnector, val appConfig: AppConfig) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  def getAgentReferenceFromEnrolments(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment("HMRC-AS-AGENT")
      .flatMap(_.getIdentifier("AgentReferenceNumber"))
      .map(_.value)

  def buildPredicate(predicate: Predicate): Predicate =
    if (appConfig.confidenceLevelConfig.authValidationEnabled) {
      predicate and ((Individual and ConfidenceLevel.L200) or Organisation or Agent)
    } else {
      predicate
    }

  def authorised(predicate: Predicate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {
    authFunction.authorised(buildPredicate(predicate)).retrieve(affinityGroup and authorisedEnrolments) {
      case Some(Individual) ~ _ =>
        val user = UserDetails("", "Individual", None)
        Future.successful(Right(user))
      case Some(Organisation) ~ _ =>
        val user = UserDetails("", "Organisation", None)
        Future.successful(Right(user))
      case Some(Agent) ~ _ =>
        retrieveAgentDetails() map {
          case arn @ Some(_) =>
            val user: AuthOutcome = Right(UserDetails("", "Agent", arn))
            user
          case None =>
            logger.warn(s"[EnrolmentsAuthService][authorised] No AgentReferenceNumber defined on agent enrolment.")
            Left(InternalError)
        }
      case _ =>
        logger.warn(s"[EnrolmentsAuthService][authorised] Invalid AffinityGroup.")
        Future.successful(Left(ClientNotAuthenticatedError))
    } recoverWith {
      case _: MissingBearerToken     => Future.successful(Left(ClientNotAuthenticatedError))
      case _: AuthorisationException => Future.successful(Left(ClientNotAuthenticatedError))
      case error =>
        logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
        Future.successful(Left(InternalError))
    }
  }

  private def retrieveAgentDetails()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
    authFunction
      .authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"))
      .retrieve(Retrievals.agentCode and Retrievals.authorisedEnrolments) {
        case _ ~ enrolments => Future.successful(getAgentReferenceFromEnrolments(enrolments))
        case _              => Future.successful(None)
      }

}

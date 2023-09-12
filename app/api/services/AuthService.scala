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
import api.models.errors.{ClientNotAuthenticatedError, InternalError, InvalidBearerTokenError}
import api.models.outcomes.AuthOutcome
import config.AppConfig
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthService @Inject() (val connector: AuthConnector, val appConfig: AppConfig) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  def authorised(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {
    authFunction.authorised(AuthProviders(AuthProvider.StandardApplication, AuthProvider.GovernmentGateway)) {
      Future.successful(Right(UserDetails("Authorised", None)))
    } recoverWith {
      case authException: AuthorisationException =>
        logger.warn(s"Auth failed. Reason: ${authException.reason}")

        authException match {
          case _: MissingBearerToken     => Future.successful(Left(InvalidBearerTokenError))
          case _: InvalidBearerToken     => Future.successful(Left(InvalidBearerTokenError))
          case _: AuthorisationException => Future.successful(Left(ClientNotAuthenticatedError))
        }

      case error =>
        logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
        Future.successful(Left(InternalError))
    }
  }

}

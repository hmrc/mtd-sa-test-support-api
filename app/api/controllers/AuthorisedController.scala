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

import api.models.auth.UserDetails
import api.models.errors._
import api.services.EnrolmentsAuthService
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

case class UserRequest[A](userDetails: UserDetails, request: Request[A]) extends WrappedRequest[A](request)

abstract class AuthorisedController(cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val authService: EnrolmentsAuthService

  def authorisedAction(): ActionBuilder[UserRequest, AnyContent] = new ActionBuilder[UserRequest, AnyContent] {

    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    override protected def executionContext: ExecutionContext = cc.executionContext

    def predicate: Predicate = Enrolment("HMRC-MTD-IT")

    override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
      implicit val headerCarrier: HeaderCarrier = hc(request)
      authService.authorised(predicate).flatMap[Result] {
        case Right(userDetails)                => block(UserRequest(userDetails, request))
        case Left(ClientNotAuthenticatedError) => Future.successful(Forbidden(Json.toJson(ClientNotAuthenticatedError)))
        case Left(_)                           => Future.successful(InternalServerError(Json.toJson(InternalError)))
      }
    }
  }
}

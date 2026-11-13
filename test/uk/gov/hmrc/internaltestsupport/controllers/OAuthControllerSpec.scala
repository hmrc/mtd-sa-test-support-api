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

package uk.gov.hmrc.internaltestsupport.controllers

import api.controllers.{ControllerBaseSpec, RequestContext}
import api.models.errors.{ErrorWrapper, InternalError, MtdError, NotFoundError}
import api.models.outcomes.ResponseWrapper
import api.services.{AuthService, ServiceOutcome}
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.internaltestsupport.models.*
import uk.gov.hmrc.internaltestsupport.models.ggauth.GGAuthResponse
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}
import uk.gov.hmrc.internaltestsupport.services.{GGAutomationService, LookupService, OAuthService}
import utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}

class OAuthControllerSpec extends ControllerBaseSpec {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  "OAuthController" should {
    "return a bad request for an invalid submission body without calling services" in new Test {
      status(post(Json.obj("identifier" -> "missing-nino"))) shouldBe Status.BAD_REQUEST
    }

    "use a supplied identifier without performing a lookup" in new Test {
      expectConfig()
      expectSignIn(Right(ResponseWrapper("", GGAuthResponse("auth-code"))))
      expectToken(Right(ResponseWrapper("", OAuthResponse("access", "refresh", 3600, "scope", "Bearer"))))

      val result = post(Json.obj("nino" -> nino, "identifier" -> identifier))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.obj("Authorization" -> "Bearer access")
    }

    "look up an identifier when it is omitted" in new Test {
      expectConfig()
      expectLookup(Right(MtdIdReference(identifier)))
      expectSignIn(Right(ResponseWrapper("", GGAuthResponse("auth-code"))))
      expectToken(Right(ResponseWrapper("", OAuthResponse("access", "refresh", 3600, "scope", "Bearer"))))

      status(post(Json.obj("nino" -> nino))) shouldBe Status.OK
    }

    Seq(
      GrantScopeRetrievalError,
      OAuthCodeRetrievalError,
      PWTimeoutError,
      PWError,
      PWTimeoutError.withMessage("timeout detail"),
      PWError.withMessage("browser detail")
    ).foreach { error =>
      s"return the mapped status and error body for ${error.code}: ${error.message}" in new Test {
        expectConfig()
        val wrapper = ErrorWrapper("correlation-id", error)
        expectSignIn(Left(wrapper))

        val result = post(Json.obj("nino" -> nino, "identifier" -> identifier))
        status(result) shouldBe (if (error.code == PWError.code || error.code == PWTimeoutError.code) 500 else 422)
        contentAsJson(result) shouldBe Json.toJson(wrapper)
      }
    }

    Seq(NotFoundError, InternalError).foreach { error =>
      s"return ${error.httpStatus} and stop when the identifier lookup fails" in new Test {
        expectConfig()
        expectLookup(Left(error))

        val result = post(Json.obj("nino" -> nino))
        status(result) shouldBe error.httpStatus
        contentAsJson(result) shouldBe error.asJson
      }
    }

    "return an internal error when the OAuth token exchange fails" in new Test {
      expectConfig()
      expectSignIn(Right(ResponseWrapper("", GGAuthResponse("auth-code"))))
      val error = ErrorWrapper("correlation-id", InternalError)
      expectToken(Left(error))

      val result = post(Json.obj("nino" -> nino, "identifier" -> identifier))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe Json.toJson(error)
    }
  }

  trait Test {
    val nino       = "AA123456A"
    val identifier = "XQIT12345678901"
    val signIn     = mockFunction[SubmitRequest, RequestContext, Future[ServiceOutcome[GGAuthResponse]]]

    val ggService = new GGAutomationService(new DefaultApplicationLifecycle, ec) {
      override def submitForm(request: SubmitRequest)(implicit rc: RequestContext): Future[ServiceOutcome[GGAuthResponse]] = signIn(request, rc)
    }

    val oauthService  = mock[OAuthService]
    val lookupService = mock[LookupService]
    val controller    = new OAuthController(cc, mock[AuthService], ggService, oauthService, lookupService, new IdGenerator)

    def expectConfig(): Unit = {
      MockAppConfig.atsAppClientId returns "client"
      MockAppConfig.atsAppClientSecret returns "secret"
    }

    def expectSignIn(outcome: ServiceOutcome[GGAuthResponse]): Unit =
      signIn.expects(SubmitRequest(nino, identifier), *).returning(Future.successful(outcome))

    def expectLookup(outcome: Either[MtdError, MtdIdReference]): Unit =
      (lookupService
        .getMtdId(_: String)(_: String, _: HeaderCarrier, _: ExecutionContext))
        .expects(nino, *, *, *)
        .returning(Future.successful(outcome))

    def expectToken(outcome: ServiceOutcome[OAuthResponse]): Unit =
      (oauthService
        .getOAuthToken(_: OAuthRequest)(_: ExecutionContext, _: RequestContext))
        .expects(OAuthRequest("authorization_code", "auth-code", "http://localhost:9000", "client", "secret"), *, *)
        .returning(Future.successful(outcome))

    def post(body: JsValue) =
      controller.post()(FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON).withBody(body))

  }

}

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

package uk.gov.hmrc.internaltestsupport.services

import api.connectors.DownstreamOutcome
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, ErrorWrapper, InternalError, OutboundError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.internaltestsupport.connectors.OAuthConnector
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

import scala.concurrent.Future

class OAuthServiceSpec extends ServiceSpec with MockFactory {

  private val connector = mock[OAuthConnector]
  private val service   = new OAuthService(connector)
  private val request   = OAuthRequest("authorization_code", "code", "http://localhost:9000", "client", "secret")
  private val response  = OAuthResponse("access", "refresh", 3600, "scope", "Bearer")

  "OAuthService" should {
    "return a successful OAuth token response unchanged" in {
      expectToken(Right(ResponseWrapper(correlationId, response)))

      await(service.getOAuthToken(request)) shouldBe Right(ResponseWrapper(correlationId, response))
    }

    "map SERVER_ERROR to InternalError" in {
      expectToken(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SERVER_ERROR")))))

      await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError, None))
    }

    Seq("INVALID_REQUEST", "invalid_grant", "invalid_client", "UNKNOWN_ERROR").foreach { code =>
      s"map downstream $code to InternalError" in {
        expectToken(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(code)))))

        await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
      }
    }

    "map an empty downstream error list to InternalError" in {
      expectToken(Left(ResponseWrapper(correlationId, DownstreamErrors(Nil))))

      await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
    }

    "preserve a parser error for an invalid downstream response" in {
      expectToken(Left(ResponseWrapper(correlationId, OutboundError(InternalError))))

      await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
    }

    "map a failed connector future to InternalError using the request correlation ID" in {
      (connector
        .getOAuthToken(_: OAuthRequest)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
        .expects(request, *, *, *)
        .returning(Future.failed(new java.net.ConnectException("connection failed")))

      await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
    }

    "map a synchronous connector exception to InternalError" in {
      (connector
        .getOAuthToken(_: OAuthRequest)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
        .expects(request, *, *, *)
        .throwing(new IllegalStateException("configuration failed"))

      await(service.getOAuthToken(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
    }
  }

  private def expectToken(outcome: DownstreamOutcome[OAuthResponse]): Unit =
    (connector
      .getOAuthToken(_: OAuthRequest)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
      .expects(request, *, *, *)
      .returning(Future.successful(outcome))

}

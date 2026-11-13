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

package uk.gov.hmrc.internaltestsupport.connectors

import api.connectors.ConnectorSpec
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, InternalError, OutboundError}
import api.models.outcomes.ResponseWrapper
import config.DownstreamConfig
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

class OAuthConnectorSpec extends ConnectorSpec {

  "OAuthConnector" when {
    Seq(
      Status.OK                  -> Json.obj("unexpected" -> "response"),
      Status.BAD_REQUEST         -> Json.obj("error" -> "invalid_grant"),
      Status.UNAUTHORIZED        -> Json.obj("error" -> "invalid_client"),
      Status.SERVICE_UNAVAILABLE -> Json.obj("error" -> "temporarily_unavailable")
    ).foreach { case (responseStatus, body) =>
      s"the downstream returns status $responseStatus with body $body" should {
        "return an internal error for an unusable token response" in new Test {
          when(POST, "/oauth/token")
            .withRequestBody(testOauthRequest.urlEncoded)
            .withHeaders(Seq(("content-type", MimeTypes.FORM)))
            .thenReturn[JsValue](status = responseStatus, body = body)

          await(connector.getOAuthToken(testOauthRequest)) shouldBe Left(ResponseWrapper("", OutboundError(InternalError)))
        }
      }
    }

    "the downstream returns a successful 200 response" when {
      "retrieving oauth token" should {
        "return the oauth response with access token" in new Test {

          when(POST, s"/oauth/token")
            .withRequestBody(testOauthRequest.urlEncoded)
            .withHeaders(Seq(("content-type", MimeTypes.FORM)))
            .thenReturn(Status.OK, oauthSuccessResponse)

          await(connector.getOAuthToken(testOauthRequest)) shouldBe Right(
            ResponseWrapper("", oauthSuccessResponse)
          )
        }
      }
    }

    "the downstream call returns an error" when {
      "the oauth token request fails" should {
        "return the corresponding error" in new Test {
          when(POST, s"/oauth/token")
            .withRequestBody(testOauthRequest.urlEncoded)
            .withHeaders(Seq(("content-type", MimeTypes.FORM)))
            .thenReturn[JsValue](status = 400, body = oauthErrorResponse)

          await(connector.getOAuthToken(testOauthRequest)) shouldBe Left(
            ResponseWrapper("", DownstreamErrors.single(DownstreamErrorCode("INVALID_REQUEST"))))
        }
      }
    }
  }

  trait Test extends ConnectorTest {

    protected val connector = new OAuthConnector(httpClientV2, mockAppConfig)

    protected val code         = "c5b544b35c36405a8e6c67d6525cb71f"
    protected val accessToken  = "test_access_token_xyz123"
    protected val refreshToken = "test_refresh_token_abc456"
    protected val tokenType    = "Bearer"
    protected val expiresIn    = 3600
    protected val scope        = "read:self-assessment write:self-assessment"

    val testOauthRequest: OAuthRequest =
      OAuthRequest(
        grant_type = "authorization_code",
        code = code,
        redirect_uri = "http://localhost:8080/callback",
        client_id = "test_client_id",
        client_secret = "test_client_secret")

    protected val oauthSuccessResponseJson: JsObject = Json.obj(
      "access_token"  -> accessToken,
      "refresh_token" -> refreshToken,
      "token_type"    -> tokenType,
      "expires_in"    -> expiresIn,
      "scope"         -> scope
    )

    protected val oauthSuccessResponse: OAuthResponse = oauthSuccessResponseJson.as[OAuthResponse]

    protected val oauthErrorResponse: JsObject = Json.obj(
      "error"  -> "invalid_request",
      "code"   -> "INVALID_REQUEST",
      "reason" -> "The request was invalid"
    )

    protected val oauthEnv         = "QA"
    protected val oauthBearerToken = "testToken"

    MockAppConfig.stubToken returns stubToken
    MockAppConfig.stubEnv returns stubEnv

    MockAppConfig.oauthDownstreamConfig returns DownstreamConfig(
      baseUrl = baseUrl,
      env = oauthEnv,
      token = oauthBearerToken,
      environmentHeaders = None)

  }

}

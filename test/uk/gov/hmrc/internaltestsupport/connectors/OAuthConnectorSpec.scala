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
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import config.DownstreamConfig
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

class OAuthConnectorSpec extends ConnectorSpec {

  "OAuthConnector" when {
    "the downstream returns a successful 200 response" when {
      "retrieving oauth token" should {
        "return the oauth response with access token" in new Test {

          when(POST, s"/oauth/token")
            .withRequestBody(Json.toJson(testOauthRequest))
            .withHeaders(Seq(("content-type", "application/json")))
            .thenReturn(OK, oauthSuccessResponse)

          await(connector.getOauthToken(testOauthRequest)) shouldBe Right(
            ResponseWrapper("", oauthSuccessResponse)
          )
        }
      }
    }

    "the downstream call returns an error" when {
      "the oauth token request fails" should {
        "return the corresponding error" in new Test {
          when(POST, s"/oauth/token")
            .withHeaders(Seq(("content-type", "application/json")))
            .thenReturn[JsValue](status = 400, body = oauthErrorResponse)

          await(connector.getOauthToken(testOauthRequest)) shouldBe Left(ResponseWrapper("", DownstreamErrors.single(DownstreamErrorCode("INVALID_REQUEST"))))
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
        grant_type = code,
        code = "authorization_code",
        redirect_uri = "http://localhost:8080/callback",
        client_id = "test_client_id",
        client_secret = "test_client_secret")

    protected val oauthSuccessResponseJson: JsObject = Json.obj(
      "accessToken"  -> accessToken,
      "refreshToken" -> refreshToken,
      "tokenType"    -> tokenType,
      "expiresIn"    -> expiresIn,
      "scope"        -> scope
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

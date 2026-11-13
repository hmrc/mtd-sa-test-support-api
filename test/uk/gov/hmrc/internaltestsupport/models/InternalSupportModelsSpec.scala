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

package uk.gov.hmrc.internaltestsupport.models

import play.api.libs.json.{Format, JsError, JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.internaltestsupport.models.ggauth.GGAuthResponse
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

class InternalSupportModelsSpec extends UnitSpec {

  "SubmitRawRequest" should {
    "read a request with an optional identifier" in {
      Json.parse("""{"nino":"AA123456A","identifier":"XQIT12345678901"}""").as[SubmitRawRequest] shouldBe
        SubmitRawRequest("AA123456A", Some("XQIT12345678901"))
    }

    "read a request without an identifier" in {
      Json.parse("""{"nino":"AA123456A"}""").as[SubmitRawRequest] shouldBe SubmitRawRequest("AA123456A")
    }
  }

  "SubmitRequest.from" should {
    "use the looked-up identifier when no identifier was supplied" in {
      SubmitRequest.from(SubmitRawRequest("AA123456A"), "looked-up-id") shouldBe SubmitRequest("AA123456A", "looked-up-id")
    }

    "prefer an identifier supplied in the request" in {
      SubmitRequest.from(SubmitRawRequest("AA123456A", Some("provided-id")), "looked-up-id") shouldBe
        SubmitRequest("AA123456A", "provided-id")
    }
  }

  "MtdIdReference" should {
    "read the MTD ID from the business details response" in {
      val json = Json.obj("success" -> Json.obj("taxPayerDisplayResponse" -> Json.obj("mtdId" -> "XQIT12345678901")))
      json.as[MtdIdReference] shouldBe MtdIdReference("XQIT12345678901")
    }

    "reject a response without an MTD ID" in {
      Json.obj("success" -> Json.obj("taxPayerDisplayResponse" -> Json.obj())).validate[MtdIdReference] shouldBe a[JsError]
    }
  }

  "OAuthRequest.urlEncoded" should {
    "encode each form value" in {
      val request = OAuthRequest("authorization_code", "code+with space", "http://localhost/callback?x=1&y=2", "client id", "secret&value")

      request.urlEncoded shouldBe
        "grant_type=authorization_code&code=code%2Bwith+space&redirect_uri=http%3A%2F%2Flocalhost%2Fcallback%3Fx%3D1%26y%3D2&client_id=client+id&client_secret=secret%26value"
    }
  }

  "OAuth token models" should {
    "round-trip the JSON field names used by the downstream service" in {
      val response = OAuthResponse("access", "refresh", 3600, "read", "Bearer")

      Json.toJson(response) shouldBe Json.obj(
        "access_token"  -> "access",
        "refresh_token" -> "refresh",
        "expires_in"    -> 3600,
        "scope"         -> "read",
        "token_type"    -> "Bearer"
      )
    }

    "serialize the GG authorization response" in {
      Json.toJson(GGAuthResponse("code")) shouldBe Json.obj("oauthCode" -> "code")
    }
  }

  private def checkFormat[A: Format](name: String, model: A, json: JsValue): Unit =
    name should {
      "write the expected JSON" in {
        Json.toJson(model) shouldBe json
      }

      "read the expected JSON" in {
        json.as[A] shouldBe model
      }

      "support a JSON round trip" in {
        Json.toJson(model).as[A] shouldBe model
      }

      "reject missing required fields" in {
        Json.obj().validate[A] shouldBe a[JsError]
      }

      "reject invalid field types" in {
        val invalid = json.as[play.api.libs.json.JsObject].fields.map { case (key, _) => key -> Json.arr() }
        play.api.libs.json.JsObject(invalid).validate[A] shouldBe a[JsError]
      }

      "reject a non-object JSON body" in {
        Json.arr().validate[A] shouldBe a[JsError]
      }
    }

  checkFormat(
    "SubmitRawRequest JSON",
    SubmitRawRequest("AA123456A", Some("mtd-id")),
    Json.obj("nino" -> "AA123456A", "identifier" -> "mtd-id")
  )

  checkFormat(
    "SubmitRequest JSON",
    SubmitRequest("AA123456A", "mtd-id"),
    Json.obj("nino" -> "AA123456A", "identifier" -> "mtd-id")
  )

  checkFormat("SubmitResponse JSON", SubmitResponse("Bearer token"), Json.obj("Authorization" -> "Bearer token"))
  checkFormat("GGAuthResponse JSON", GGAuthResponse("code"), Json.obj("oauthCode" -> "code"))

  checkFormat(
    "OAuthRequest JSON",
    OAuthRequest("authorization_code", "code", "http://localhost:9000", "client", "secret"),
    Json.obj(
      "grant_type"    -> "authorization_code",
      "code"          -> "code",
      "redirect_uri"  -> "http://localhost:9000",
      "client_id"     -> "client",
      "client_secret" -> "secret"
    )
  )

  checkFormat(
    "OAuthResponse JSON",
    OAuthResponse("access", "refresh", 3600, "read", "Bearer"),
    Json.obj("access_token" -> "access", "refresh_token" -> "refresh", "expires_in" -> 3600, "scope" -> "read", "token_type" -> "Bearer")
  )

}

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

package uk.gov.hmrc.internaltestsupport.models.oauth

import play.api.libs.json.{Json, OFormat}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

case class OAuthRequest(grant_type: String, code: String, redirect_uri: String, client_id: String, client_secret: String) {

  private def encode(value: String): String = URLEncoder.encode(value, UTF_8)

  val urlEncoded: String = {
    s"grant_type=${encode(grant_type)}" +
      s"&code=${encode(code)}" +
      s"&redirect_uri=${encode(redirect_uri)}" +
      s"&client_id=${encode(client_id)}" +
      s"&client_secret=${encode(client_secret)}"
  }

}

object OAuthRequest {
  implicit val format: OFormat[OAuthRequest] = Json.format[OAuthRequest]
}

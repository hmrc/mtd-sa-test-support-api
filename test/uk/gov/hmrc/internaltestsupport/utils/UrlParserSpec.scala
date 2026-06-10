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

package uk.gov.hmrc.internaltestsupport.utils

import support.UnitSpec

class UrlParserSpec extends UnitSpec {

  "UrlParser.extractCode" should {
    "extract code from query string" in {
      val url = "http://localhost:9000/callback?code=abc123&state=1"
      UrlParser.extractCode(url) shouldBe Right("abc123")
    }

    "extract code from fragment" in {
      val url = "http://localhost:9000/callback#code=frag123&state=1"
      UrlParser.extractCode(url) shouldBe Right("frag123")
    }

    "decode percent-encoded code value" in {
      val url = "http://localhost:9000/callback?code=abc%20123"
      UrlParser.extractCode(url) shouldBe Right("abc 123")
    }

    "find code when embedded in other text (fallback)" in {
      val s = "<html><body>redirecting... http://localhost:9000/callback?code=xyz789&state=1</body></html>"
      UrlParser.extractCode(s) shouldBe Right("xyz789")
    }

    "handle complex percent-encoded characters" in {
      val url = "http://localhost:9000/callback?code=abc%20def%2F456"
      UrlParser.extractCode(url) shouldBe Right("abc def/456")
    }

    "throw RuntimeException when no code is found" in {
      val url = "http://localhost:9000/callback?state=1&other=value"
      the[RuntimeException] thrownBy {
        UrlParser.extractCode(url)
      } should have message "No OAuth code found in URL: http://localhost:9000/callback?state=1&other=value"
    }

    "prioritize query string over fragment" in {
      val url = "http://localhost:9000/callback?code=fromquery#code=fromfragment"
      UrlParser.extractCode(url) shouldBe Right("fromquery")
    }

    "extract code from fragment when query is absent" in {
      val url = "http://localhost:9000/callback#state=1&code=fragcode&extra=value"
      UrlParser.extractCode(url) shouldBe Right("fragcode")
    }
  }

}

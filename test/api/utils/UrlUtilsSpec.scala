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

package api.utils

import api.utils.UrlUtils.appendQueryParams

class UrlUtilsSpec extends UnitSpec {

  "appendQueryParams" when {
    "given a URL with no query params of its own" when {
      "given an empty queryParams list" should {
        "return an unchanged URL" in {
          appendQueryParams("http://something/else", Nil) shouldBe "http://something/else"
        }
      }

      "given a non-empty queryParams list" should {
        "return a URL with added queryParams" in {
          appendQueryParams("http://something/else", List("taxYear" -> "23-24")) shouldBe "http://something/else?taxYear=23-24"
        }
      }
    }

    "given a URL with query params of its own" when {
      "given an empty queryParams list" should {
        "return an unchanged URL" in {
          appendQueryParams("http://something/else?alreadyGot=this", Nil) shouldBe "http://something/else?alreadyGot=this"
        }
      }

      "given a non-empty queryParams list" should {
        "return a URL with added queryParams" in {
          appendQueryParams(
            "http://something/else?alreadyGot=this",
            List("taxYear" -> "23-24")) shouldBe "http://something/else?alreadyGot=this&taxYear=23-24"
        }
      }
    }
  }

}

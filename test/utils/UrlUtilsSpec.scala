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

package utils

import support.UnitSpec
import utils.UrlUtils.appendQueryParams

class UrlUtilsSpec extends UnitSpec {

  val url               = "http://something/else"
  val urlWithQueryParam = "http://something/else?alreadyGot=this"

  "appendQueryParams" when {
    "given an empty queryParams list" should {
      "return an unchanged URL" in {
        val result = appendQueryParams(url, Nil)
        result shouldBe url
      }
    }

    "given a URL with no query params of its own" should {
      "return a URL with added queryParams" in {
        val queryParams = List("taxYear" -> "23-24")
        val result      = appendQueryParams(url, queryParams)
        result shouldBe "http://something/else?taxYear=23-24"
      }
    }

    "given a URL with query params" should {
      "return a URL with no extra queryParams" in {
        val result = appendQueryParams(urlWithQueryParam, Nil)
        result shouldBe urlWithQueryParam
      }
    }

    "given a URL with query params" should {
      "return a URL with added queryParams" in {
        val queryParams = List("taxYear" -> "23-24")
        val result      = appendQueryParams(urlWithQueryParam, queryParams)
        result shouldBe "http://something/else?alreadyGot=this&taxYear=23-24"
      }
    }
  }
}

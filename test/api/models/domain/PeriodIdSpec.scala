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

package api.models.domain

import play.api.libs.json.{Json, JsString}
import support.UnitSpec

class PeriodIdSpec extends UnitSpec {

  "PeriodId" when {
    "being created from a single string" should {
      "create a blank from & to if the string is the wrong length" in {
        val result = PeriodId("invalid-string")
        result.from shouldBe ""
        result.to shouldBe ""
      }

      "populate from & to" in {
        val result = PeriodId("2017-04-06_2017-07-04")
        result.from shouldBe "2017-04-06"
        result.to shouldBe "2017-07-04"
      }
    }

    "being created using to and from values" should {
      "create a valid PeriodId" in {
        val result = PeriodId(from = "2017-04-06", to = "2017-07-04")
        result.value shouldBe "2017-04-06_2017-07-04"
      }
    }

    "serialized to JSON" must {
      "serialize the embedded value as a string" in {
        val value = "2017-04-06_2017-07-04"
        Json.toJson(PeriodId(value)) shouldBe JsString(value)
      }
    }
  }
}

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

package api.controllers.requestParsers.validators.validations

import api.models.errors.MtdError
import support.UnitSpec

import scala.util.matching.Regex

class RegexValidationSpec extends UnitSpec {

  val error = new MtdError("SOME_ERROR", "Some msg", 400)

  object Validation extends RegexValidation {
    override val regex: Regex = "ABC".r
  }

  "RegexValidation" must {
    "return no errors" when {
      "the regex matches fully" in {
        Validation.validateRegex("ABC", error) shouldBe Nil
      }
    }

    "return the error" when {
      "the regex does not match" in {
        Validation.validateRegex("BCD", error) shouldBe Seq(error)
      }

      "the regex only matches partially" in {
        Validation.validateRegex("AB", error) shouldBe Seq(error)
      }
    }
  }

}

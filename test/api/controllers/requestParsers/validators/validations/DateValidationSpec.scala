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

import api.models.errors.DateFormatError
import support.UnitSpec

class DateValidationSpec extends UnitSpec {
  private val error = DateFormatError

  "DateValidation" must {
    "return no errors" when {
      "the supplied value has the correct format and represents a real date" in {
        DateValidation.validate("2020-01-01", error) shouldBe Nil
      }
    }

    "return the requested error" when {
      "the supplied value has the wrong date format" in {
        DateValidation.validate("2021-1-1", error) shouldBe Seq(error)
      }

      "the supplied value is not a date" in {
        DateValidation.validate("XXXX", error) shouldBe Seq(error)
      }

      "the supplied value has the correct format but does not represent a real date" in {
        DateValidation.validate("2021-13-13", error) shouldBe Seq(error)
      }
    }
  }

}

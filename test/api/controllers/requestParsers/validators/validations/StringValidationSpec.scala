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

import api.models.errors.StringFormatError
import support.UnitSpec

class StringValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "a valid string is provided" in {
        val string           = "valid string"
        val validationResult = StringValidation.validate(string, "path")
        validationResult.isEmpty shouldBe true
      }
    }
    "return StringFormatError error" when {
      "an invalid name is provided" in {
        val string           = "valid string *"
        val validationResult = StringValidation.validate(string, "path")
        validationResult.length shouldBe 1
        validationResult.head shouldBe StringFormatError.copy(paths = Some(Seq("path")))
      }
    }
  }

  "validateOptional" should {
    "return no errors" when {
      "a valid string is provided" in {
        val string           = "valid string"
        val validationResult = StringValidation.validateOptional(Some(string), "path")
        validationResult.isEmpty shouldBe true
      }
      "no string is provided" in {
        val validationResult = StringValidation.validateOptional(None, "path")
        validationResult.isEmpty shouldBe true
      }
    }
    "return StringFormatError error" when {
      "an invalid name is provided" in {
        val string           = "valid string *"
        val validationResult = StringValidation.validateOptional(Some(string), "path")
        validationResult.length shouldBe 1
        validationResult.head shouldBe StringFormatError.copy(paths = Some(Seq("path")))
      }
    }
  }
}

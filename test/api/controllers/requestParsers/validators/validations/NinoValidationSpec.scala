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

import api.models.errors.NinoFormatError
import api.utils.JsonErrorValidators
import support.UnitSpec

class NinoValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when a valid NINO is supplied" in {

        val validNino        = "AA123456A"
        val validationResult = NinoValidation.validate(validNino)
        validationResult.isEmpty shouldBe true

      }
    }

    "return an error" when {
      "when an invalid NINO is supplied" in {

        val invalidNino      = "AA123456ABCBBCBCBC"
        val validationResult = NinoValidation.validate(invalidNino)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe NinoFormatError

      }
    }

  }

}

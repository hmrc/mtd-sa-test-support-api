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

import api.models.errors.{FromDateFormatError, ToDateFormatError}
import support.UnitSpec

class DateValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "when a valid fromDate is supplied" in {

        val validDate        = "2020-01-01"
        val validationResult = DateValidation.validate(validDate, isFromDate = true)
        validationResult.isEmpty shouldBe true

      }

      "when a valid toDate is supplied" in {

        val validDate        = "2020-03-12"
        val validationResult = DateValidation.validate(validDate, isFromDate = true)
        validationResult.isEmpty shouldBe true

      }
    }
    "return an error" when {
      "when an invalid fromDate is supplied" in {

        val invalidBusinessId = "01-01-2020"
        val validationResult  = DateValidation.validate(invalidBusinessId, isFromDate = true)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe FromDateFormatError

      }

      "when an invalid toDate is supplied" in {

        val invalidBusinessId = "30-01-2020"
        val validationResult  = DateValidation.validate(invalidBusinessId, isFromDate = false)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ToDateFormatError

      }
    }
  }

}

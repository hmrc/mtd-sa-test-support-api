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

import api.models.errors.SubmissionIdFormatError
import support.UnitSpec

class SubmissionIdValidationSpec extends UnitSpec {
  "validate" should {
    "return no errors" when {
      "a valid businessId is passed in" in {
        SubmissionIdValidation.validate("12345678-1234-4123-9123-123456789012") shouldBe Nil
      }
    }
    "return an error" when {
      "an invalid businessId is passed in" in {
        SubmissionIdValidation.validate("12345678-1234-4123-9123-1234567890123") shouldBe List(SubmissionIdFormatError)
      }
    }
  }
}

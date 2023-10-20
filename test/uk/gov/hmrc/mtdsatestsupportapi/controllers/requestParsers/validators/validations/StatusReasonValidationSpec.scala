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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.models.errors.StatusReasonFormatError
import support.UnitSpec

class StatusReasonValidationSpec extends UnitSpec {

  "StatusReasonValidation" should {
    "return no error" when {
      checkValid("00")
      checkValid("01")
      checkValid("02")
      checkValid("03")
      checkValid("04")
      checkValid("05")
      checkValid("06")
      checkValid("07")
      checkValid("08")
      checkValid("09")

      def checkValid(value: String): Unit =
        s"provided with a string of '$value'" in {
          StatusReasonValidation.validate(value) shouldBe Nil
        }
    }
    "return an error" when {
      "invalid value is provided" in {
        StatusReasonValidation.validate("99") shouldBe List(StatusReasonFormatError)
      }
    }
  }

}

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
      checkValid("Sign up - return available")
      checkValid("Sign up - no return available")
      checkValid("ITSA final declaration")
      checkValid("ITSA Q4 declaration")
      checkValid("CESA SA return")
      checkValid("Complex")
      checkValid("Ceased income source")
      checkValid("Reinstated income source")
      checkValid("Rollover")
      checkValid("Income Source Latency Changes")
      checkValid("MTD ITSA Opt-Out")

      def checkValid(value: String): Unit =
        s"provided with a string of '$value'" in {
          StatusReasonValidation.validate(value) shouldBe Nil
        }
    }
    "return an error" when {
      "invalid value is provided" in {
        StatusReasonValidation.validate("Unsupported reason") shouldBe List(StatusReasonFormatError)
      }
    }
  }

}

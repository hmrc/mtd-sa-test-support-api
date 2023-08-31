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

import api.models.errors.AccountingTypeFormatError
import support.UnitSpec

class AccountingTypeValidationSpec extends UnitSpec {

  "validate" should {

    "return no errors" when {
      checkValid("CASH")
      checkValid("ACCRUALS")

      def checkValid(value: String): Unit =
        s"provided with a string of '$value'" in {
          AccountingTypeValidation.validate(value) shouldBe Nil
        }
    }

    "return a the correct error" when {
      "provided with an unknown value" in {
        AccountingTypeValidation.validate("invalid") shouldBe List(AccountingTypeFormatError)
      }
    }
  }
}


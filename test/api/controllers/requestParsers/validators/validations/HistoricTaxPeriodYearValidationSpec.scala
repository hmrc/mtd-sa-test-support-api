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

import api.models.errors.{RuleHistoricTaxYearNotSupportedError, TaxYearFormatError}
import support.UnitSpec

class HistoricTaxPeriodYearValidationSpec extends UnitSpec {

  val minTaxYear = 2017
  val maxTaxYear = 2021
  val taxDate    = "2020-08-02"

  "validate" should {
    "return no errors" when {
      "a valid taxDate is supplied" in {
        val result = HistoricTaxPeriodYearValidation.validate(minTaxYear, maxTaxYear, taxDate)

        result shouldBe Nil
      }
      "the minimum taxDate is supplied" in {
        val result = HistoricTaxPeriodYearValidation.validate(minTaxYear, maxTaxYear, "2017-10-01")

        result shouldBe Nil
      }
    }
    "return an error" when {
      "a taxDate with an invalid format is supplied" in {
        val result = HistoricTaxPeriodYearValidation.validate(minTaxYear, maxTaxYear, "2019-20-A1")

        result shouldBe List(TaxYearFormatError)
      }
      "a taxDate before the minimum is supplied" in {
        val result = HistoricTaxPeriodYearValidation.validate(minTaxYear, maxTaxYear, "2014-11-10")

        result shouldBe List(RuleHistoricTaxYearNotSupportedError)
      }
      "a taxDate after the maximum is supplied" in {
        val result = HistoricTaxPeriodYearValidation.validate(minTaxYear, maxTaxYear, "2022-08-05")

        result shouldBe List(RuleHistoricTaxYearNotSupportedError)
      }
    }
  }
}

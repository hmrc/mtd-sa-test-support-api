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

import api.models.errors._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import support.UnitSpec

class TaxYearValidationSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks {

  val taxYear    = "2022-23"
  val minTaxYear = 2021
  val maxTaxYear = 2025

  "validate" should {
    val validation = TaxYearValidation.validate(minTaxYear, _)
    behave like taxYearValidation(validation)

    "return RuleTaxYearNotSupportedError" when {
      "a taxYear before the minimum is supplied" in {
        validation("2020-21") should contain only RuleTaxYearNotSupportedError
      }
    }

    "return no errors" when {
      "the tax year supplied is the minimum allowed" in {
        validation("2021-22") shouldBe empty
      }
    }
  }

  "validateHistoric" should {
    val validation = TaxYearValidation.validateHistoric(minTaxYear, maxTaxYear, _)
    behave like taxYearValidation(validation)

    "return RuleHistoricTaxYearNotSupportedError" when {
      "a taxYear before the minimum is supplied" in {
        validation("2020-21") should contain only RuleHistoricTaxYearNotSupportedError
      }
    }

    "return RuleHistoricTaxYearNotSupportedError" when {
      "a taxYear after the maximum is supplied" in {
        validation("2026-27") should contain only RuleHistoricTaxYearNotSupportedError
      }
    }

    "return no errors" when {
      "the tax year supplied is the minimum allowed" in {
        validation("2021-22") shouldBe empty
      }
    }

    "return no errors" when {
      "the tax year supplied is the maximum allowed" in {
        validation("2025-26") shouldBe empty
      }
    }
  }

  def taxYearValidation(validation: String => List[MtdError]): Unit = {

    "return no errors" when {
      "a valid taxYear is supplied" in {
        validation(taxYear) shouldBe empty
      }
    }

    "return TaxYearFormatError" when {
      "something that is not a tax year range is supplied" in {
        validation("2019") should contain only TaxYearFormatError
      }

      "an empty string is supplied" in {
        validation("") should contain only TaxYearFormatError
      }

      "a non-numeric range is supplied" in {
        validation("XXXX-YY") should contain only TaxYearFormatError
      }

      "a taxYear range with an invalid format is supplied" in {
        validation("2019/20") should contain only TaxYearFormatError
      }
    }

    "return RuleTaxYearRangeInvalidError" when {

      "a taxYear with a range longer than 1 is supplied" in {
        // To prove that the the invalid range check works regardless of any min/max supported tax year checks
        // (subject to matching the format regex):
        forAll(Gen.choose(10, 97)) { i: Int =>
          val taxYear = "20%02d-%02d".format(i, i + 2)
          validation(taxYear) should contain only RuleTaxYearRangeInvalidError
        }
      }
    }
  }

}

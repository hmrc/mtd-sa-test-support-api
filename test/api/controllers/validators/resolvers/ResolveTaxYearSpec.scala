/*
 * Copyright 2026 HM Revenue & Customs
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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

class ResolveTaxYearSpec extends UnitSpec {

  "ResolveTaxYear" should {
    "return no errors" when {
      val validTaxYear: String = "2026-27"

      "given a valid tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = ResolveTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }

      "given a valid tax year in an Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTaxYear(Option(validTaxYear))
        result shouldBe Valid(Some(TaxYear.fromMtd(validTaxYear)))
      }

      "given an empty Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTaxYear(None)
        result shouldBe Valid(None)
      }
    }

    "return an error" when {
      "given an invalid tax year format" in {
        ResolveTaxYear("2026") shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a tax year string in which the range is greater than 1 year" in {
        ResolveTaxYear("2026-28") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the end year is before the start year" in {
        ResolveTaxYear("2026-25") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the start and end years are the same" in {
        ResolveTaxYear("2026-26") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the tax year is bad" in {
        ResolveTaxYear("20266-26") shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

  "ResolveDetailedTaxYear" should {
    val minimumTaxYear: TaxYear = TaxYear.fromMtd("2026-27")
    val currentTaxYear: TaxYear = TaxYear.currentTaxYear

    def resolver(allowIncompleteTaxYear: Boolean = true, maxTaxYear: Option[TaxYear] = None): ResolveDetailedTaxYear = ResolveDetailedTaxYear(
      minimumTaxYear = minimumTaxYear,
      maximumTaxYear = maxTaxYear,
      allowIncompleteTaxYear = allowIncompleteTaxYear
    )

    val defaultResolver: ResolveDetailedTaxYear = ResolveDetailedTaxYear(minimumTaxYear = minimumTaxYear)

    "return no errors" when {
      "given the minimum allowed tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2026-27")
        result shouldBe Valid(minimumTaxYear)
      }

      "given an incomplete tax year but incomplete years are allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()(currentTaxYear.asMtd)
        result shouldBe Valid(currentTaxYear)
      }

      "given the minimum allowed tax year using default arguments" in {
        val result: Validated[Seq[MtdError], TaxYear] = defaultResolver("2026-27")
        result shouldBe Valid(minimumTaxYear)
      }

      "given an incomplete tax year when incomplete years are allowed by default" in {
        val result: Validated[Seq[MtdError], TaxYear] = defaultResolver(currentTaxYear.asMtd)
        result shouldBe Valid(currentTaxYear)
      }

      "given a valid tax year when no maximum tax year is configured" in {
        val result: Validated[Seq[MtdError], TaxYear] = defaultResolver("2028-29")
        result shouldBe Valid(TaxYear.fromMtd("2028-29"))
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given a tax year before the minimum tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2025-26")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }

      "given a tax year after the maximum tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver(maxTaxYear = Some(TaxYear.fromMtd("2027-28")))("2028-29")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotEndedError" when {
      "given an incomplete tax year and incomplete years are not allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver(false)(currentTaxYear.asMtd)
        result shouldBe Invalid(List(RuleTaxYearNotEndedError))
      }
    }

    "return TaxYearFormatError" when {
      "given a badly formatted tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("not-a-tax-year")
        result shouldBe Invalid(List(TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "given a tax year with an invalid range" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2026-28")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }
    }
  }

}

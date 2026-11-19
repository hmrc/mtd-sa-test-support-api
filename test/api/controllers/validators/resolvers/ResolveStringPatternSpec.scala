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

import api.models.errors.{MtdError, TaxYearFormatError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import scala.util.matching.Regex

class ResolveStringPatternSpec extends UnitSpec {

  private val taxYearRegex: Regex = "20[1-9][0-9]-[1-9][0-9]".r

  "ResolveStringPattern" should {
    "return no errors" when {
      "given a matching string" in {
        val result: Validated[Seq[MtdError], String] = ResolveStringPattern("2026-27", taxYearRegex, TaxYearFormatError)

        result shouldBe Valid("2026-27")
      }

      "given a matching string in an Option" in {
        val result: Validated[Seq[MtdError], Option[String]] = ResolveStringPattern(Some("2026-27"), taxYearRegex, TaxYearFormatError)

        result shouldBe Valid(Some("2026-27"))
      }

      "given an empty Option" in {
        val result: Validated[Seq[MtdError], Option[String]] = ResolveStringPattern(None, taxYearRegex, TaxYearFormatError)

        result shouldBe Valid(None)
      }
    }

    "return the expected error" when {
      "given a non-matching string" in {
        val result: Validated[Seq[MtdError], String] = ResolveStringPattern("does-not-match-regex", taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a non-matching string in an Option" in {
        val result: Validated[Seq[MtdError], Option[String]] = ResolveStringPattern(Some("does-not-match-regex"), taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "given an empty string" in {
        val result: Validated[Seq[MtdError], String] = ResolveStringPattern("", taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "given an empty string in an Option" in {
        val result: Validated[Seq[MtdError], Option[String]] = ResolveStringPattern(Some(""), taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a string with only spaces" in {
        val result: Validated[Seq[MtdError], String] = ResolveStringPattern("   ", taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a string with only spaces in an Option" in {
        val result: Validated[Seq[MtdError], Option[String]] = ResolveStringPattern(Some("   "), taxYearRegex, TaxYearFormatError)

        result shouldBe Invalid(List(TaxYearFormatError))
      }
    }

  }

}

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

import api.controllers.validators.resolvers.ResolveParsedAlpha2CountryCode.permittedCustomCodes
import api.models.errors.{CountryCodeFormatError, MtdError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import com.neovisionaries.i18n.CountryCode

class ResolveParsedAlpha2CountryCodeSpec extends UnitSpec {

  private val validCountryCodes: Seq[String] = (CountryCode.values.map(_.getAlpha2).filter(_.length == 2).toSet ++ permittedCustomCodes).toSeq.sorted

  "ResolveParsedAlpha2CountryCode" should {
    validCountryCodes.foreach { code =>
      s"return valid for a valid country code $code" in {
        val result: Validated[Seq[MtdError], String] = ResolveParsedAlpha2CountryCode(code, "path")

        result shouldBe Valid(code)
      }
    }

    "return valid for an empty optional country code" in {
      val result: Validated[Seq[MtdError], Option[String]] = ResolveParsedAlpha2CountryCode(None, "path")

      result shouldBe Valid(None)
    }

    "return valid for a valid optional country code" in {
      val result: Validated[Seq[MtdError], Option[String]] = ResolveParsedAlpha2CountryCode(Some("US"), "path")

      result shouldBe Valid(Some("US"))
    }

    "return a CountryCodeFormatError for a badly formatted country code" in {
      val result: Validated[Seq[MtdError], String] = ResolveParsedAlpha2CountryCode("FRANCE", "path")

      result shouldBe Invalid(List(CountryCodeFormatError.withPath("path")))
    }

    "return a CountryCodeFormatError for a badly formatted optional country code" in {
      val result: Validated[Seq[MtdError], Option[String]] = ResolveParsedAlpha2CountryCode(Some("FRANCE"), "path")

      result shouldBe Invalid(List(CountryCodeFormatError.withPath("path")))
    }

    "return a CountryCodeFormatError for an invalid ISO 3166-1 alpha-2 country code" in {
      val result: Validated[Seq[MtdError], String] = ResolveParsedAlpha2CountryCode("AB", "path")

      result shouldBe Invalid(List(CountryCodeFormatError.forRuleCountryCode.withPath("path")))
    }

    "return a CountryCodeFormatError for an invalid ISO 3166-1 alpha-2 optional country code" in {
      val result: Validated[Seq[MtdError], Option[String]] = ResolveParsedAlpha2CountryCode(Some("AB"), "path")

      result shouldBe Invalid(List(CountryCodeFormatError.forRuleCountryCode.withPath("path")))
    }
  }

}

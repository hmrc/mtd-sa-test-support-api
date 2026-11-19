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

package api.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors.MtdError
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import play.api.http.Status.BAD_REQUEST

class RulesValidatorSpec extends UnitSpec {

  private val validNino    = Nino("AA123456A")
  private val validTaxYear = TaxYear.currentTaxYear()

  private case class TestParsedRequest(nino: Nino, taxYear: TaxYear, valid1: Boolean, valid2: Boolean)
  private object Valid1Error extends MtdError("INVALID_1", "Field valid1 is invalid", BAD_REQUEST)
  private object Valid2Error extends MtdError("INVALID_2", "Field valid2 is invalid", BAD_REQUEST)

  private val testRulesValidator = new RulesValidator[TestParsedRequest] {

    def validateBusinessRules(parsed: TestParsedRequest): Validated[Seq[MtdError], TestParsedRequest] = {
      val resolved1 = if (parsed.valid1) valid else Invalid(List(Valid1Error))
      val resolved2 = if (parsed.valid2) valid else Invalid(List(Valid2Error))
      combine(resolved1, resolved2).toUnit.onSuccess(parsed)
    }

  }

  "RulesValidator" should {
    "combine valid results and return the parsed object" in {
      val parsed = TestParsedRequest(validNino, validTaxYear, valid1 = true, valid2 = true)
      val result = testRulesValidator.validateBusinessRules(parsed)
      result shouldBe Valid(parsed)
    }

    "catch a single invalid result and return the error" in {
      val parsed = TestParsedRequest(validNino, validTaxYear, valid1 = false, valid2 = true)
      val result = testRulesValidator.validateBusinessRules(parsed)
      result shouldBe Invalid(List(Valid1Error))
    }

    "catch multiple invalid results and return the errors" in {
      val parsed = TestParsedRequest(validNino, validTaxYear, valid1 = false, valid2 = false)
      val result = testRulesValidator.validateBusinessRules(parsed)
      result shouldBe Invalid(List(Valid1Error, Valid2Error))
    }
  }

}

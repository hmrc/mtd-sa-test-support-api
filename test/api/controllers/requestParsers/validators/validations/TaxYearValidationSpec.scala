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

import api.models.errors.{RuleTaxYearRangeInvalidError, TaxYearFormatError}
import support.UnitSpec

class TaxYearValidationSpec extends UnitSpec {

  "validate" must {
    "return no errors" when {
      "a valid taxYear is supplied" in {
        TaxYearValidation.validate("2021-22") shouldBe Nil
      }
    }

    "return an error" when {
      "a taxYear with an invalid format is supplied" in {
        TaxYearValidation.validate("2021") shouldBe Seq(TaxYearFormatError)
      }

      "a taxYear with a range longer than 1 is supplied" in {
        TaxYearValidation.validate("2021-23") shouldBe Seq(RuleTaxYearRangeInvalidError)
      }
    }
  }

  "validate" when {
    "a path is supplied" must {
      val path = "somePath"

      "return no errors" when {
        "a valid taxYear is supplied" in {
          TaxYearValidation.validate("2021-22", path) shouldBe Nil
        }
      }

      "return an error with the path" when {
        "a taxYear with an invalid format is supplied" in {
          TaxYearValidation.validate("2021", path) shouldBe Seq(TaxYearFormatError.withExtraPath(path))
        }

        "a taxYear with a range longer than 1 is supplied" in {
          TaxYearValidation.validate("2021-23", path) shouldBe Seq(RuleTaxYearRangeInvalidError.withExtraPath(path))
        }
      }
    }
  }

}

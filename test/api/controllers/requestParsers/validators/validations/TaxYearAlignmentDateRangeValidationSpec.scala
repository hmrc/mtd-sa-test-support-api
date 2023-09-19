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

import api.models.errors.MtdError
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec

import java.time.LocalDate

class TaxYearAlignmentDateRangeValidationSpec extends UnitSpec {

  private val error = new MtdError("SOME_CODE", "Some message", BAD_REQUEST)

  private def validate(start: String, end: String) = {
    def date(string: String): LocalDate = LocalDate.parse(string)
    TaxYearAlignmentDateRangeValidation.validate(date(start), date(end), error)
  }

  "TaxYearAlignmentDateRangeValidation" when {
    "date range runs from 6 Apr to 5 Apr following year" must {
      "return no errors" in {
        validate("2023-04-06", "2024-04-05") shouldBe Nil
      }
    }

    "date range runs from 6 Apr to 5 Apr but more than one year later" must {
      "return the error" in {
        validate("2023-04-06", "2025-04-05") shouldBe Seq(error)
      }
    }

    "the start date is after the end date" must {
      "return the error" in {
        validate("2025-04-05", "2023-04-06") shouldBe Seq(error)
      }
    }

    "date range runs from 6 Apr to something other than 5 Apr the following year" must {
      "return the error" in {
        validate("2023-04-06", "2024-04-03") shouldBe Seq(error)
      }
    }

    "date range runs from something other than 6 Apr to 5 Apr the following year" must {
      "return the error" in {
        validate("2023-04-07", "2024-04-05") shouldBe Seq(error)
      }
    }

    "date range uses other dates spanning a full year" must {
      "return the error" in {
        validate("2023-01-01", "2023-12-31") shouldBe Seq(error)
      }
    }
  }

}

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

import api.models.errors.PeriodIdFormatError
import support.UnitSpec

class HistoricPeriodIdValidationSpec extends UnitSpec {

  val minTaxYear = 2017
  val maxTaxYear = 2021

  "validate" should {
    "return no errors" when {
      "a valid periodId is supplied" in {
        val periodId = "2019-04-06_2019-07-04"
        val result   = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, periodId)

        result shouldBe Nil
      }
      "the minimum taxDate is supplied" in {
        val minPeriodId = "2017-04-06_2017-07-04"
        val result      = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, minPeriodId)

        result shouldBe Nil
      }
      "the maximum taxDate is supplied" in {
        val maxPeriodId = "2021-04-06_2021-07-04"
        val result      = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, maxPeriodId)

        result shouldBe Nil
      }
    }
    "return an error" when {
      "a periodId with an invalid format is supplied" in {
        val invalidPeriodId = "2017-04-06__2017-07-04"
        val result          = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, invalidPeriodId)

        result shouldBe List(PeriodIdFormatError)
      }
      "a periodId with an invalid date format is supplied" in {
        val invalidPeriodId = "20A7-04-06_2017-07-04"
        val result          = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, invalidPeriodId)

        result shouldBe List(PeriodIdFormatError)
      }
      "a periodId before the minimum is supplied" in {
        val earlyPeriodId = "2016-04-06_2016-07-04"
        val result        = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, earlyPeriodId)

        result shouldBe List(PeriodIdFormatError)
      }
      "a periodId after the maximum is supplied" in {
        val latePeriodId = "2022-04-06_2022-07-04"
        val result       = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, latePeriodId)

        result shouldBe List(PeriodIdFormatError)
      }
      "a periodId with toDate before fromDate is supplied" in {
        val latePeriodId = "2022-07-04_2022-04-06"
        val result       = HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, latePeriodId)

        result shouldBe List(PeriodIdFormatError)
      }
    }
  }
}

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

import api.models.errors.{MtdError, PeriodIdFormatError}

object HistoricPeriodIdValidation {

  def validate(minimumTaxYear: Int, maximumTaxYear: Int, periodId: String): List[MtdError] = {

    val periodIdLength = "YYYY-MM-DD_YYYY-MM-DD".length

    if (periodId.length.equals(periodIdLength)) {

      val fromDate   = periodId.substring(0, 10)
      val toDate     = periodId.substring(11, 21)
      val underscore = periodId.substring(10, 11)

      val historicDateErrors = HistoricTaxPeriodYearValidation.validate(minimumTaxYear, maximumTaxYear, fromDate) ++
        HistoricTaxPeriodYearValidation.validate(minimumTaxYear, maximumTaxYear, toDate)

      if (historicDateErrors.equals(NoValidationErrors)) {
        val dateOrderErrors = ToDateBeforeFromDateValidation.validate(fromDate, toDate)

        if (dateOrderErrors.equals(Nil)) {
          if (underscore.matches("_")) {
            NoValidationErrors
          } else {
            List(PeriodIdFormatError)
          }
        } else {
          List(PeriodIdFormatError)
        }
      } else {
        List(PeriodIdFormatError)
      }
    } else {
      List(PeriodIdFormatError)
    }
  }

}

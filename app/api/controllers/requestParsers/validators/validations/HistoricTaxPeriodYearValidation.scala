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

import api.models.errors.{MtdError, RuleHistoricTaxYearNotSupportedError, TaxYearFormatError}

object HistoricTaxPeriodYearValidation {

  val taxDateFormat = "20[1-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]"

  def validate(minimumTaxYear: Int, maximumTaxYear: Int, taxYear: String): List[MtdError] = {
    if (taxYear.matches(taxDateFormat)) {

      val year = taxYear.substring(0, 4).toInt

      if (year >= minimumTaxYear && year <= maximumTaxYear) {
        NoValidationErrors
      } else {
        List(RuleHistoricTaxYearNotSupportedError)
      }

    } else {
      List(TaxYearFormatError)
    }

  }

}

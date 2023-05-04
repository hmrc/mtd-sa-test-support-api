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

import java.time.LocalDate

import api.models.errors.{ MtdError, RuleToDateBeforeFromDateError }

object ToDateBeforeFromDateValidation {

  def validate(from: String, to: String): List[MtdError] = {

    val fromDate = LocalDate.parse(from, dateFormat)
    val toDate   = LocalDate.parse(to, dateFormat)

    if (toDate.isBefore(fromDate)) List(RuleToDateBeforeFromDateError) else Nil

  }

}

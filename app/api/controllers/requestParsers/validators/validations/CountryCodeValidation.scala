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

import api.models.errors.{CountryCodeFormatError, MtdError, RuleCountryCodeError}
import com.neovisionaries.i18n.CountryCode

object CountryCodeValidation {

  private val permittedCustomCodes = Set("ZZZ")

  def validate(field: String, path: String): List[MtdError] = {
    if (field.length != 3) {
      List(CountryCodeFormatError.copy(paths = Some(Seq(path))))
    } else if (permittedCustomCodes.contains(field)) {
      NoValidationErrors
    } else {
      Option(CountryCode.getByAlpha3Code(field)) match {
        case Some(_) => NoValidationErrors
        case None    => List(RuleCountryCodeError.copy(paths = Some(Seq(path))))
      }
    }
  }

}

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

import api.models.errors.{MtdError, ValueFormatError}

object NumberValidation {

  def validateOptional(field: Option[BigDecimal], path: String, min: BigDecimal = 0, max: BigDecimal = 99999999999.99): List[MtdError] = {
    field match {
      case None        => NoValidationErrors
      case Some(value) => validate(value, path, min, max)
    }
  }

  def validate(field: BigDecimal, path: String, min: BigDecimal = 0, max: BigDecimal = 99999999999.99): List[MtdError] = {
    if (field >= min && field <= max && field.scale <= 2) {
      Nil
    } else {
      List(
        ValueFormatError.forPathAndRange(path, min.toString, max.toString)
      )
    }
  }

}

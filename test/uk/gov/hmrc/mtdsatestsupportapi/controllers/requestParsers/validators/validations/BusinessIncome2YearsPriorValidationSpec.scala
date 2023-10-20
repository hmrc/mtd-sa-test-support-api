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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.models.errors.BusinessIncome2YearsPriorFormatError
import support.UnitSpec

class BusinessIncome2YearsPriorValidationSpec extends UnitSpec {
  private val error = BusinessIncome2YearsPriorFormatError

  "BusinessIncome2YearsPriorValidation" must {
    "return no errors" when {
      "a valid value is provided" in {
        BusinessIncome2YearsPriorValidation.validateOptional(Some(2000), path = "/businessIncome2YearsPrior") shouldBe Nil
      }
    }
    "return an error" when {
      "invalid value is provided" in {
        BusinessIncome2YearsPriorValidation.validateOptional(Some(-1000), path = "/businessIncome2YearsPrior") shouldBe List(error.withExtraPath("/businessIncome2YearsPrior"))
      }
    }
  }

}

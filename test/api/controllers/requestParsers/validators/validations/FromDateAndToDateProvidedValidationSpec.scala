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

import api.models.errors.{MissingFromDateError, MissingToDateError}
import support.UnitSpec

class FromDateAndToDateProvidedValidationSpec extends UnitSpec {
  "validation" should {
    "return no errors" when {
      "no dates are provided" in {
        FromDateAndToDateProvidedValidation.validate(None, None) shouldBe Nil
      }
      "both dates are provided" in {
        FromDateAndToDateProvidedValidation.validate(Some(""), Some("")) shouldBe Nil
      }
    }
    "return an error" when {
      "only fromDate is provided" in {
        FromDateAndToDateProvidedValidation.validate(Some(""), None) shouldBe List(MissingToDateError)
      }
      "only toDate is provided" in {
        FromDateAndToDateProvidedValidation.validate(None, Some("")) shouldBe List(MissingFromDateError)
      }
    }
  }
}

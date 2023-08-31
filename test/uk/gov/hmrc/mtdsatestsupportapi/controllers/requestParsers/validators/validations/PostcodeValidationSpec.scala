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

import api.models.errors.PostcodeFormatError
import support.UnitSpec

private class PostcodeValidationSpec extends UnitSpec {
        val error = PostcodeFormatError

  // Not exhaustive. These are sanity check only.
  "PostcodeValidation" must {
    "return no errors" when {
      "a valid postcode is supplied" in {
        PostcodeValidation.validate("SW1A 1AA", error) shouldBe Nil
      }
    }

    "return the error" when {
      "something that is not a postcode is supplied" in {
        PostcodeValidation.validate("Not a postcode", error) shouldBe Seq(error)
      }
    }
  }

}

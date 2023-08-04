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

import api.models.errors.CheckpointIdFormatError
import support.UnitSpec

class CheckpointIdValidationSpec extends UnitSpec {

  "CheckpointIdValidation" when {
    "given a checkpointId of invalid format" must {
      "return a checkpointId format error" in {
        val invalidId = "some_invalid_id"

        CheckpointIdValidation.validate(invalidId) shouldBe List(CheckpointIdFormatError)
      }
    }
    "given a valid checkpointId format" must {
      "return no errors" in {
        val validId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

        CheckpointIdValidation.validate(validId) shouldBe NoValidationErrors
      }
    }
  }

}

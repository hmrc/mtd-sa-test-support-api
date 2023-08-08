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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import api.models.errors.CheckpointIdFormatError
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteCheckpoint.DeleteCheckpointRawData

class DeleteCheckpointValidatorSpec extends UnitSpec {

  private val validator = new DeleteCheckpointValidator

  private val validCheckpointId   = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val invalidCheckpointId = "some_invalid_id"

  private val validRawData   = DeleteCheckpointRawData("some_id", validCheckpointId)
  private val invalidRawData = DeleteCheckpointRawData("some_id", invalidCheckpointId)

  "DeleteCheckpointValidator" when {
    "validating raw data that contains a valid checkpoint id" should {
      "return no errors" in {
        validator.validate(validRawData) shouldBe NoValidationErrors
      }
    }
    "validating raw data that contains an invalid checkpoint id" should {
      "return a CheckpointIdFormatError" in {
        validator.validate(invalidRawData) shouldBe List(CheckpointIdFormatError)
      }
    }
  }

}

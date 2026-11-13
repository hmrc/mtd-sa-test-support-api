/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.restoreCheckpoint

import api.models.domain.CheckpointId
import api.models.errors.{CheckpointIdFormatError, ErrorWrapper}
import api.utils.UnitSpec
import v1.restoreCheckpoint.model.request.RestoreCheckpointRequestData

class RestoreCheckpointValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val vendorClientId: String    = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
  private val validCheckpointId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private def validate(checkpointId: String) = new RestoreCheckpointValidator(vendorClientId, checkpointId).validateAndWrapResult()

  "RestoreCheckpointValidator" should {
    "return the parsed object" when {
      "a valid request is supplied" in {
        validate(validCheckpointId) shouldBe Right(RestoreCheckpointRequestData(vendorClientId, CheckpointId(validCheckpointId)))
      }
    }

    "return CheckpointIdFormatError" when {
      "an invalid checkpoint ID is supplied" in {
        validate("a1e8057e") shouldBe Left(ErrorWrapper(correlationId, CheckpointIdFormatError))
      }
    }
  }

}

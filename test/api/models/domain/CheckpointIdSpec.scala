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

package api.models.domain

import support.UnitSpec

class CheckpointIdSpec extends UnitSpec {

  "The validation of a checkpointId" must {
    "fail when given in an invalid format" in {
      val invalidCheckpointId = "some_invalid_id"

      CheckpointId.isValid(invalidCheckpointId) shouldBe false
    }
    "fail when the checkpointId is null" in {
      CheckpointId.isValid(null) shouldBe false
    }
    "pass when given in a valid format" in {
      val validCheckpointId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

      CheckpointId.isValid(validCheckpointId) shouldBe true
    }
  }

}

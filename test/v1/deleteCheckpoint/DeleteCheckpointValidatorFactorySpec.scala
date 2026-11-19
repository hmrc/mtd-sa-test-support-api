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

package v1.deleteCheckpoint

import api.controllers.validators.Validator
import api.utils.UnitSpec
import v1.deleteCheckpoint.model.request.DeleteCheckpointRequestData

class DeleteCheckpointValidatorFactorySpec extends UnitSpec {

  private val validatorFactory: DeleteCheckpointValidatorFactory = new DeleteCheckpointValidatorFactory

  "DeleteCheckpointValidatorFactory" should {
    "return DeleteCheckpointValidator" in {
      val result: Validator[DeleteCheckpointRequestData] = validatorFactory.validator(
        vendorClientId = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7",
        checkpointId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
      )

      result shouldBe a[DeleteCheckpointValidator]
    }
  }

}

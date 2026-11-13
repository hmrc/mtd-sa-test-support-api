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

package api.controllers.validators.resolvers

import api.models.domain.CheckpointId
import api.models.errors.{CheckpointIdFormatError, MtdError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

class ResolveCheckpointIdSpec extends UnitSpec {

  "ResolveCheckpointId" should {
    "return no errors" when {
      "given a valid checkpoint ID" in {
        val validCheckpointId: String                      = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
        val result: Validated[Seq[MtdError], CheckpointId] = ResolveCheckpointId(validCheckpointId)

        result shouldBe Valid(CheckpointId(validCheckpointId))
      }
    }

    "return an error" when {
      "given an invalid checkpoint ID" in {
        val invalidCheckpointId: String                    = "a1e8057e"
        val result: Validated[Seq[MtdError], CheckpointId] = ResolveCheckpointId(invalidCheckpointId)

        result shouldBe Invalid(List(CheckpointIdFormatError))
      }
    }
  }

}

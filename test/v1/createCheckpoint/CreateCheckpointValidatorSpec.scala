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

package v1.createCheckpoint

import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, NinoFormatError}
import api.utils.UnitSpec
import v1.createCheckpoint.model.request.CreateCheckpointRequestData

class CreateCheckpointValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
  private val nino: String           = "AA123456A"

  private def validate(nino: Option[String]) = new CreateCheckpointValidator(vendorClientId, nino).validateAndWrapResult()

  "CreateCheckpointValidator" should {
    "return the parsed object" when {
      "a valid request with query parameter nino is supplied" in {
        validate(Some(nino)) shouldBe Right(CreateCheckpointRequestData(vendorClientId, Nino(nino)))
      }
    }

    "return NinoFormatError" when {
      "an invalid request without query parameter nino is supplied" in {
        validate(None) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an invalid nino is supplied" in {
        validate(Some("AA1234")) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }

}

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

import api.models.errors.NinoFormatError
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRawData

class CreateCheckpointValidatorSpec extends UnitSpec {

  private val vendorClientId = "some_id"
  private val validator = new CreateCheckpointValidator()

  "CreateCheckpointValidator when validating" should {
    "return no errors" when {
      "a valid request with nino is supplied" in {
        validator.validate(CreateCheckpointRawData(vendorClientId, Some("AA123456A"))) shouldBe Nil
      }
    }

    // TODO this is temporary while we decide whether to allow missing nino.
    // Will either remove or replace with more specific error.
    "return NinoFormatError error" when {
      "no nino supplied" in {
        validator.validate(CreateCheckpointRawData(vendorClientId, None)) shouldBe List(NinoFormatError)
      }
    }

    "return NinoFormatError error" when {
      "a bad nino is supplied" in {
        validator.validate(CreateCheckpointRawData(vendorClientId, Some("BAD_NINO"))) shouldBe List(NinoFormatError)
      }
    }
  }

}

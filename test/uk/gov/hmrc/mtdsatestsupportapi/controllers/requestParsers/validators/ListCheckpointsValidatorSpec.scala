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
import api.models.errors.NinoFormatError
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRawData

class ListCheckpointsValidatorSpec extends UnitSpec {

  val validator = new ListCheckpointsValidator

  "ListCheckpointsValidator" when {
    "validating a valid nino" should {
      "return no errors" in {
        val validNino = "TC663795B"
        val rawData   = ListCheckpointsRawData("some_vendor_id", Some(validNino))

        validator.validate(rawData) shouldBe NoValidationErrors
      }
    }
    "validating an invalid nino" should {
      "return NinoFormatError" in {
        val invalidNino = "invalid_nino"
        val rawData     = ListCheckpointsRawData("some_vendor_id", Some(invalidNino))

        validator.validate(rawData) shouldBe List(NinoFormatError)
      }
    }
    "no nino is present to validate" should {
      "return no errors" in {
        val rawData = ListCheckpointsRawData("some_vendor_id", None)

        validator.validate(rawData) shouldBe NoValidationErrors
      }
    }
  }

}

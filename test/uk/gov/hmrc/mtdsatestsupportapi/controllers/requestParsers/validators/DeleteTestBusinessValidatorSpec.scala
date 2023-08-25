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
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.DeleteTestBusinessRawData

class DeleteTestBusinessValidatorSpec extends UnitSpec {

  val validator = new DeleteTestBusinessValidator
  val vendorClientId = "some_id"

  "DeleteTestBusinessValidator" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(DeleteTestBusinessRawData(vendorClientId, "TC663795B")) shouldBe Nil
      }
    }
    "return a NinoFormatError" when {
      "the request contains an invalid nino" in {
        validator.validate(DeleteTestBusinessRawData(vendorClientId, "not_a_valid_nino")) shouldBe List(NinoFormatError)

      }
    }
  }
}

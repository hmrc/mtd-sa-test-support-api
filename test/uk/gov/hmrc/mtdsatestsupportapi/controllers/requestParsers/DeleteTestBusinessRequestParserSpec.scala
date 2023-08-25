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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers

import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, NinoFormatError}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockDeleteTestBusinessValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.{DeleteTestBusinessRawData, DeleteTestBusinessRequest}

class DeleteTestBusinessRequestParserSpec extends UnitSpec with MockDeleteTestBusinessValidator {

  val parser  = new DeleteTestBusinessRequestParser(mockDeleteTestBusinessValidator)

  implicit val correlationId: String = "X-123"

  "DeleteTestBusinessRequestParser" must {
    "parse clean rawData into a request" in {
      val rawData = DeleteTestBusinessRawData("some_vendor_id", "AA999999A")

      MockDeleteTestBusinessValidator.validate(rawData).returns(Nil)

      parser.parseRequest(rawData) shouldBe Right(DeleteTestBusinessRequest("some_vendor_id", Nino("AA999999A")))

    }
    "return an error" when {
      "rawData contains an invalid Nino" in {
        val rawData = DeleteTestBusinessRawData("some_vendor_id", "not_a_valid_nino")

        MockDeleteTestBusinessValidator.validate(rawData).returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))

      }
    }
  }

}

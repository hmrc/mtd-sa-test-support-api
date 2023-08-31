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
import api.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockDeleteTestBusinessValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.{DeleteTestBusinessRawData, DeleteTestBusinessRequest}

class DeleteTestBusinessRequestParserSpec extends UnitSpec with MockDeleteTestBusinessValidator {

  val parser = new DeleteTestBusinessRequestParser(mockDeleteTestBusinessValidator)

  implicit val correlationId: String = "X-123"
  val nino                           = "AA999999A"
  val businessId                     = "XAIS12345678910"
  val rawData                        = DeleteTestBusinessRawData( nino, businessId)

  "DeleteTestBusinessRequestParser" must {
    "parse clean rawData into a request" in {

      MockDeleteTestBusinessValidator.validate(rawData).returns(Nil)

      parser.parseRequest(rawData) shouldBe Right(DeleteTestBusinessRequest( Nino(nino), businessId))

    }
    "return an error" when {
      "a single validation error occurs" in {

        MockDeleteTestBusinessValidator.validate(rawData).returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))

      }

      "many validation errors occur" in {

        MockDeleteTestBusinessValidator.validate(rawData).returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }

}

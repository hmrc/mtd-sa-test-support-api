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

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import api.models.domain.Nino
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, StringFormatError}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockListCheckpointsValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.{ListCheckpointsRawData, ListCheckpointsRequest}

class ListCheckpointsRequestParserSpec extends UnitSpec with MockListCheckpointsValidator {

  protected implicit val correlationId: String = "X-123"
  protected val vendorClientId                 = "some_vendor_id"
  protected val validNino                      = "TC663795B"
  protected val invalidNino                    = "invalid_nino"

  protected val validRawData: ListCheckpointsRawData       = ListCheckpointsRawData(vendorClientId, Some(validNino))
  protected val validRawDataNoNino: ListCheckpointsRawData = ListCheckpointsRawData(vendorClientId, None)

  protected val invalidRawData: ListCheckpointsRawData = ListCheckpointsRawData(vendorClientId, Some(invalidNino))

  protected val parser = new ListCheckpointsRequestParser(mockListCheckpointsValidator)

  "ListCheckpointsRequestParser" when {
    "parsing valid raw data with a nino" should {
      "generate the corresponding request object" in {
        MockListCheckpointsValidator
          .validate(validRawData)
          .returns(NoValidationErrors)

        parser.parseRequest(validRawData) shouldBe Right(ListCheckpointsRequest(vendorClientId, Some(Nino(validNino))))
      }
    }
    "parsing valid raw data without a nino" should {
      "generate the corresponding request object" in {
        MockListCheckpointsValidator
          .validate(validRawDataNoNino)
          .returns(NoValidationErrors)

        parser.parseRequest(validRawDataNoNino) shouldBe Right(ListCheckpointsRequest(vendorClientId, None))
      }
    }
    "parsing invalid raw data" when {
      "one error is returned from the validator" should {
        "return that error" in {
          MockListCheckpointsValidator
            .validate(invalidRawData)
            .returns(List(NinoFormatError))

          parser.parseRequest(invalidRawData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
        }
      }
      "multiple errors are returned from the validator" should {
        "return all the errors" in {
          MockListCheckpointsValidator
            .validate(invalidRawData)
            .returns(List(NinoFormatError, StringFormatError))

          parser.parseRequest(invalidRawData) shouldBe Left(
            ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, StringFormatError))))
        }
      }
    }
  }

}

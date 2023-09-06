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
import api.models.errors._
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateTestBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockCreateTestBusinessValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.{CreateTestBusinessRawData, CreateTestBusinessRequest}

class CreateTestBusinessRequestParserSpec extends UnitSpec with MockCreateTestBusinessValidator with CreateTestBusinessFixtures {

  private implicit val correlationId: String = "X-123"

  private val rawData = CreateTestBusinessRawData("AA123456A", MinimalCreateTestBusinessRequest.mtdBusinessJson)

  private val parser = new CreateTestBusinessRequestParser(mockCreateTestBusinessValidator)

  "CreateTestBusinessRequestParser" should {
    "return a request object" when {
      "valid request data is supplied" in {
        MockCreateTestBusinessValidator.validate(rawData) returns Nil

        parser.parseRequest(rawData) shouldBe
          Right(CreateTestBusinessRequest(Nino("AA123456A"), MinimalCreateTestBusinessRequest.business))
      }
    }

    "return an error" when {
      "a single validation error occurs" in {
        MockCreateTestBusinessValidator.validate(rawData) returns List(NinoFormatError)

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in {
        MockCreateTestBusinessValidator.validate(rawData) returns List(StringFormatError, NinoFormatError)

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(StringFormatError, NinoFormatError))))
      }
    }
  }

}

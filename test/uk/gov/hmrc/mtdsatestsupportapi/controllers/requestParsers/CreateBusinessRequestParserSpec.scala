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
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockCreateBusinessValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness.{CreateBusinessRawData, CreateBusinessRequest}

class CreateBusinessRequestParserSpec extends UnitSpec with MockCreateBusinessValidator with CreateBusinessFixtures {

  private implicit val correlationId: String = "X-123"

  private val rawData = CreateBusinessRawData("AA123456A", MinimalCreateBusinessRequest.mtdBusinessJson)

  private val parser = new CreateBusinessRequestParser(mockCreateBusinessValidator)

  "CreateBusinessRequestParser" should {
    "return a request object" when {
      "valid request data is supplied" in {
        MockCreateBusinessValidator.validate(rawData) returns Nil

        parser.parseRequest(rawData) shouldBe
          Right(CreateBusinessRequest(Nino("AA123456A"), MinimalCreateBusinessRequest.business))
      }
    }

    "return an error" when {
      "a single validation error occurs" in {
        MockCreateBusinessValidator.validate(rawData) returns List(NinoFormatError)

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in {
        MockCreateBusinessValidator.validate(rawData) returns List(StringFormatError, NinoFormatError)

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(StringFormatError, NinoFormatError))))
      }
    }
  }

}

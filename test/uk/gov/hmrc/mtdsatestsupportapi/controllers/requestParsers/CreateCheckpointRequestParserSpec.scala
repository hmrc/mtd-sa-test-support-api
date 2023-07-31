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
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockCreateCheckpointValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.{CreateCheckpointRawData, CreateCheckpointRequest}

class CreateCheckpointRequestParserSpec extends UnitSpec with MockCreateCheckpointValidator {

  private val vendorClientId                 = "some_id"
  private val nino                           = "TC663795B"
  private implicit val correlationId: String = "X-123"

  private val parser = new CreateCheckpointRequestParser(mockValidator)

  "CreateCheckpointRequestParser" should {
    val data = CreateCheckpointRawData(vendorClientId, Some(nino))

    "return a request object" when {
      "valid request data is supplied" in {
        MockCreateCheckpointValidator.validate(data).returns(Nil)

        parser.parseRequest(data) shouldBe Right(CreateCheckpointRequest(vendorClientId, Nino(nino)))
      }
    }

    "return an error" when {
      "a single validation error occurs" in {
        MockCreateCheckpointValidator.validate(data).returns(List(NinoFormatError))

        parser.parseRequest(data) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in {
        MockCreateCheckpointValidator.validate(data).returns(List(StringFormatError, NinoFormatError))

        parser.parseRequest(data) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(StringFormatError, NinoFormatError))))
      }
    }
  }

}

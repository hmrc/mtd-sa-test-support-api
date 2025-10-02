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

import api.models.domain.CheckpointId
import api.models.errors.*
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockRestoreCheckpointValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.restoreCheckpoint.{RestoreCheckpointRawData, RestoreCheckpointRequest}

class RestoreCheckpointRequestParserSpec extends UnitSpec with MockRestoreCheckpointValidator {

  private val vendorClientId                 = "some_client_id"
  private val validCheckpointId              = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val invalidCheckpointId            = "some_invalid_id"
  private implicit val correlationId: String = "X-123"

  private val validRawData   = RestoreCheckpointRawData(vendorClientId, validCheckpointId)
  private val invalidRawData = RestoreCheckpointRawData(vendorClientId, invalidCheckpointId)

  private val parser = new RestoreCheckpointRequestParser(mockRestoreCheckpointValidator)

  "RestoreCheckpointRequestParser" should {
    "return a request object" when {
      "valid request data is supplied" in {
        MockRestoreCheckpointValidator.validate(validRawData).returns(Nil)

        parser.parseRequest(validRawData) shouldBe Right(RestoreCheckpointRequest(vendorClientId, CheckpointId(validCheckpointId)))
      }
    }

    "return an error" when {
      "a single validation error occurs" in {
        MockRestoreCheckpointValidator.validate(invalidRawData).returns(List(CheckpointIdFormatError))

        parser.parseRequest(invalidRawData) shouldBe Left(ErrorWrapper(correlationId, CheckpointIdFormatError, None))
      }

      "multiple validation errors occur" in {
        MockRestoreCheckpointValidator.validate(invalidRawData).returns(List(StringFormatError, CheckpointIdFormatError))

        parser.parseRequest(invalidRawData) shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(Seq(StringFormatError, CheckpointIdFormatError))))
      }
    }
  }

}

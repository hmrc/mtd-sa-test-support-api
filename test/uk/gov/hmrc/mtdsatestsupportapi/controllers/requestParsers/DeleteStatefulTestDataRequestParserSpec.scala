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

import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, VendorClientIdFormatError}
import play.api.libs.json._
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockDeleteStatefulTestDataValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.VendorClientId
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.{DeleteStatefulTestDataRawData, DeleteStatefulTestDataRequest}

class DeleteStatefulTestDataRequestParserSpec extends UnitSpec {

  val vendorClientId = "some_id"
  val requestBody: JsObject = Json.obj("exampleBody" -> "someValue")
  implicit val correlationId: String = "X-123"

  "DeleteStatefulTestDataRequestParser" should {
    val data = DeleteStatefulTestDataRawData(vendorClientId, Some(requestBody))
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeleteStatefulTestDataValidator.validate(data).returns(Nil)

        parser.parseRequest(data) shouldBe Right(DeleteStatefulTestDataRequest(VendorClientId(vendorClientId), Some(requestBody)))
      }
    }
    "return an error" when {
      "a single validation error occurs" in new Test {
        MockDeleteStatefulTestDataValidator.validate(data).returns(List(VendorClientIdFormatError))

        parser.parseRequest(data) shouldBe Left(ErrorWrapper(correlationId, VendorClientIdFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockDeleteStatefulTestDataValidator.validate(data).returns(List(VendorClientIdFormatError, NinoFormatError))

        parser.parseRequest(data) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(VendorClientIdFormatError, NinoFormatError))))
      }
    }
  }

  trait Test extends MockDeleteStatefulTestDataValidator {
    lazy val parser = new DeleteStatefulTestDataRequestParser(mockValidator)
  }


}

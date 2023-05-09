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

import play.api.libs.json._
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockDeleteStatefulTestDataValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRawData

class DeleteStatefulTestDataValidatorSpec extends UnitSpec {

  val vendorClientId = "some_id"
  val requestBody: JsObject = Json.obj("exampleBody" -> "someValue")

  "DeleteStatefulTestDataValidator when validating" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(DeleteStatefulTestDataRawData(vendorClientId, Some(requestBody))) shouldBe Nil
      }
    }
  }

  trait Test extends MockDeleteStatefulTestDataValidator {
    lazy val validator = new DeleteStatefulTestDataValidator()
  }
}
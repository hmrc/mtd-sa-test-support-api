/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.createTestBusiness.model.request

import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.*
import v1.createTestBusiness.model.request.TypeOfBusiness.*

class CreateTestBusinessRequestBodySpec extends UnitSpec {

  "CreateTestBusinessRequestBody" when {
    Seq(
      (mtdSelfEmploymentRequestBodyJson, maxSelfEmploymentRequestBodyModel),
      (mtdPropertyRequestBodyJson(`uk-property`), maxPropertyRequestBodyModel(`uk-property`)),
      (mtdPropertyRequestBodyJson(`foreign-property`), maxPropertyRequestBodyModel(`foreign-property`)),
      (mtdPropertyRequestBodyJson(`property-unspecified`), maxPropertyRequestBodyModel(`property-unspecified`))
    ).foreach { (requestBodyJson, requestBodyModel) =>
      s"read from valid Json for typeOfBusiness: ${requestBodyModel.typeOfBusiness}" should {
        "produce the expected object" in {
          requestBodyJson.as[CreateTestBusinessRequestBody] shouldBe requestBodyModel
        }
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        JsObject.empty.validate[CreateTestBusinessRequestBody] shouldBe a[JsError]
      }
    }

    Seq(
      (maxSelfEmploymentRequestBodyModel, downstreamSelfEmploymentRequestBodyJson),
      (maxPropertyRequestBodyModel(`uk-property`), downstreamPropertyRequestBodyJson("ukProperty", Some("02"))),
      (maxPropertyRequestBodyModel(`foreign-property`), downstreamPropertyRequestBodyJson("foreignProperty", Some("03"))),
      (maxPropertyRequestBodyModel(`property-unspecified`), downstreamPropertyRequestBodyJson("ukProperty"))
    ).foreach { (requestBodyModel, requestBodyJson) =>
      s"written to Json for typeOfBusiness: ${requestBodyModel.typeOfBusiness}" should {
        "produce the expected JsObject" in {
          Json.toJson(requestBodyModel) shouldBe requestBodyJson
        }
      }
    }

    "hasAnyBusinessAddressDetails" should {
      "return true when any business address details are present" in {
        maxSelfEmploymentRequestBodyModel.hasAnyBusinessAddressDetails shouldBe true
      }

      "return false when all business address details are absent" in {
        minPropertyRequestBodyModel(`uk-property`).hasAnyBusinessAddressDetails shouldBe false
      }
    }
  }

}

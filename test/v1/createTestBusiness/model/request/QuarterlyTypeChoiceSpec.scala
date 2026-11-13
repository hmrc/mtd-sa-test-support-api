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

class QuarterlyTypeChoiceSpec extends UnitSpec {

  "QuarterlyTypeChoice" when {
    "read from valid Json" should {
      "produce the expected object" in {
        mtdQuarterlyTypeChoiceJson.as[QuarterlyTypeChoice] shouldBe quarterlyTypeChoiceModel
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        JsObject.empty.validate[QuarterlyTypeChoice] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(quarterlyTypeChoiceModel) shouldBe downstreamQuarterlyTypeChoiceJson
      }
    }
  }

}

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

package v1.createAmendItsaStatus.model.request

import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.*

class ItsaStatusDetailSpec extends UnitSpec {

  private val downstreamItsaStatusDetailJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2024-03-23T16:02:34.039Z",
      |  "status": "01",
      |  "statusReason": "00",
      |  "businessIncomePriorTo2Years": 4500.23
      |}
    """.stripMargin
  )

  "ItsaStatusDetail" when {
    "read from valid Json" should {
      "produce the expected object" in {
        mtdItsaStatusDetailJson.as[ItsaStatusDetail] shouldBe itsaStatusDetailModel
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        JsObject.empty.validate[ItsaStatusDetail] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(itsaStatusDetailModel) shouldBe downstreamItsaStatusDetailJson
      }
    }
  }

}

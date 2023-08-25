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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness

import play.api.libs.json.{JsString, Json}
import support.UnitSpec

class LatencyIndicatorSpec extends UnitSpec {

  "LatencyIndicator" when {
    "deserialized from API JSON" must {
      "work" in {
        JsString("Annual").as[LatencyIndicator] shouldBe LatencyIndicator.Annual
        JsString("Quarterly").as[LatencyIndicator] shouldBe LatencyIndicator.Quarterly
      }
    }

    "serialized to downstream JSON" must {
      "work" in {
        Json.toJson[LatencyIndicator](LatencyIndicator.Annual) shouldBe JsString("A")
        Json.toJson[LatencyIndicator](LatencyIndicator.Quarterly) shouldBe JsString("Q")
      }
    }
  }

}

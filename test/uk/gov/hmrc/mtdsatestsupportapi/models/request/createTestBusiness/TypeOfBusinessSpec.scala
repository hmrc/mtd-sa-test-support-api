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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness

import play.api.libs.json.JsString
import support.UnitSpec

class TypeOfBusinessSpec extends UnitSpec {

  "TypeOfBusiness" when {
    "deserialized from API JSON" must {
      "work" in {
        JsString("uk-property").as[TypeOfBusiness] shouldBe TypeOfBusiness.`uk-property`
        JsString("foreign-property").as[TypeOfBusiness] shouldBe TypeOfBusiness.`foreign-property`
        JsString("self-employment").as[TypeOfBusiness] shouldBe TypeOfBusiness.`self-employment`
        JsString("property-unspecified").as[TypeOfBusiness] shouldBe TypeOfBusiness.`property-unspecified`
      }
    }
  }

}

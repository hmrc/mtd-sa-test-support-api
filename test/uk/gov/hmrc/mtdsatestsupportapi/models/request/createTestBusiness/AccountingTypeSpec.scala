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

import play.api.libs.json.{JsString, Json}
import support.UnitSpec

class AccountingTypeSpec extends UnitSpec {

  "AccountingType" when {
    "deserialized from API JSON" must {
      "work" in {
        JsString("CASH").as[AccountingType] shouldBe AccountingType.CASH
        JsString("ACCRUALS").as[AccountingType] shouldBe AccountingType.ACCRUALS
      }
    }

    "serialized to downstream JSON" must {
      "work" in {
        Json.toJson[AccountingType](AccountingType.CASH) shouldBe JsString("CASH")
        Json.toJson[AccountingType](AccountingType.ACCRUALS) shouldBe JsString("ACCRUAL")
      }
    }
  }

}

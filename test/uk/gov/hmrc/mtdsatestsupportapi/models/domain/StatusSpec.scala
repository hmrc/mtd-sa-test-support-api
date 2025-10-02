/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mtdsatestsupportapi.models.domain

import play.api.libs.json.*
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.Status.*
import utils.enums.EnumJsonSpecSupport

class StatusSpec extends UnitSpec with EnumJsonSpecSupport {

  "Status" when {
    testDeserialization[Status](
      ("No Status", `No Status`),
      ("MTD Mandated", `MTD Mandated`),
      ("MTD Voluntary", `MTD Voluntary`),
      ("Annual", Annual),
      ("Non Digital", `Non Digital`),
      ("Dormant", Dormant),
      ("MTD Exempt", `MTD Exempt`)
    )

    testSerialization[Status](
      (`No Status`, "00"),
      (`MTD Mandated`, "01"),
      (`MTD Voluntary`, "02"),
      (Annual, "03"),
      (`Non Digital`, "04"),
      (Dormant, "05"),
      (`MTD Exempt`, "99")
    )

    "reading an invalid Status" should {
      "return a JsError" in {
        Json.fromJson[Status](JsString("021")) shouldBe JsError(JsPath, JsonValidationError("error.expected.Status"))
      }
    }
  }

}

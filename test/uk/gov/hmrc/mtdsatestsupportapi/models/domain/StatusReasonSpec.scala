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

import play.api.libs.json._
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReason._
import utils.enums.EnumJsonSpecSupport

class StatusReasonSpec extends UnitSpec with EnumJsonSpecSupport {

  "StatusReason" when {
    testDeserialization[StatusReason](
      ("Sign up - return available", `Sign up - return available`),
      ("Sign up - no return available", `Sign up - no return available`),
      ("ITSA final declaration", `ITSA final declaration`),
      ("ITSA Q4 declaration", `ITSA Q4 declaration`),
      ("CESA SA return", `CESA SA return`),
      ("Complex", Complex),
      ("Ceased income source", `Ceased income source`),
      ("Reinstated income source", `Reinstated income source`),
      ("Rollover", Rollover),
      ("Income Source Latency Changes", `Income Source Latency Changes`),
      ("MTD ITSA Opt-Out", `MTD ITSA Opt-Out`),
      ("MTD ITSA Opt-In", `MTD ITSA Opt-In`),
      ("Digitally Exempt", `Digitally Exempt`)
    )

    testSerialization[StatusReason](
      (`Sign up - return available`, "00"),
      (`Sign up - no return available`, "01"),
      (`ITSA final declaration`, "02"),
      (`ITSA Q4 declaration`, "03"),
      (`CESA SA return`, "04"),
      (Complex, "05"),
      (`Ceased income source`, "06"),
      (`Reinstated income source`, "07"),
      (Rollover, "08"),
      (`Income Source Latency Changes`, "09"),
      (`MTD ITSA Opt-Out`, "10"),
      (`MTD ITSA Opt-In`, "11"),
      (`Digitally Exempt`, "12")
    )

    "reading an invalid StatusReason" should {
      "return a JsError" in {
        Json.fromJson[StatusReason](JsString("011")) shouldBe
          JsError(JsPath, JsonValidationError("error.expected.StatusReason"))
      }
    }
  }

}

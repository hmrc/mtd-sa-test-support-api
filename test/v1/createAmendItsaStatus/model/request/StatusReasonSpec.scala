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
import v1.createAmendItsaStatus.model.request.StatusReason.*

class StatusReasonSpec extends UnitSpec {

  private val testCases: Seq[(StatusReason, String)] = Seq(
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

  "StatusReason" when {
    "toDownstream" should {
      "return the expected downstream string value" in {
        testCases.foreach { (statusReason, expectedDownstreamValue) =>
          statusReason.toDownstream shouldBe expectedDownstreamValue
        }
      }
    }
  }

}

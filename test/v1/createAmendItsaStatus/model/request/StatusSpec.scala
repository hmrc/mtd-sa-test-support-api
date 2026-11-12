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
import v1.createAmendItsaStatus.model.request.Status.*

class StatusSpec extends UnitSpec {

  private val testCases: Seq[(Status, String)] = Seq(
    (`No Status`, "00"),
    (`MTD Mandated`, "01"),
    (`MTD Voluntary`, "02"),
    (Annual, "03"),
    (`Non Digital`, "04"),
    (Dormant, "05"),
    (`MTD Exempt`, "99")
  )

  "Status" when {
    "toDownstream" should {
      "return the expected downstream string value" in {
        testCases.foreach { (status, expectedDownstreamValue) =>
          status.toDownstream shouldBe expectedDownstreamValue
        }
      }
    }
  }

}

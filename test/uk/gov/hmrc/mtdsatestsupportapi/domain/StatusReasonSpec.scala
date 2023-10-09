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

package uk.gov.hmrc.mtdsatestsupportapi.domain

import play.api.libs.json._
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReason
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReason._

class StatusReasonSpec extends UnitSpec {

  private val allStatusReasons: List[StatusReason] = List(`00`, `01`, `02`, `03`, `04`, `05`, `06`, `07`, `08`, `09`)

  "StatusReason" should {
    allStatusReasons.foreach { statusReason =>
      s"read $statusReason from JSON correctly" in {
        val json: JsValue                  = JsString(statusReason.toString)
        val result: JsResult[StatusReason] = Json.fromJson[StatusReason](json)

        result shouldBe JsSuccess(statusReason)
      }

      s"write $statusReason to downstream format correctly" in {
        val downstreamJson: JsValue = JsString(statusReason.downstreamValue)
        Json.toJson(statusReason) shouldBe downstreamJson
      }
    }

    "return a JsError" when {
      "reading an invalid StatusReason" in {
        val json: JsValue                  = JsString("011")
        val result: JsResult[StatusReason] = Json.fromJson[StatusReason](json)

        result shouldBe JsError(JsPath, JsonValidationError("error.expected.StatusReason"))
      }
    }
  }

}

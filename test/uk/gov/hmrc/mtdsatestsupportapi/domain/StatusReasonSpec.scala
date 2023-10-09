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

import play.api.libs.json.{JsString, JsSuccess, Json, Writes}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReason
import utils.enums.EnumJsonSpecSupport

class StatusReasonSpec extends UnitSpec with EnumJsonSpecSupport {

  implicit val writes: Writes[StatusReason] = (obj: StatusReason) => JsString(obj.downstreamValue)

  val allStatusReason: List[StatusReason] = List(
    StatusReason.`00`,
    StatusReason.`01`,
    StatusReason.`02`,
    StatusReason.`03`,
    StatusReason.`04`,
    StatusReason.`05`,
    StatusReason.`06`,
    StatusReason.`07`,
    StatusReason.`08`,
    StatusReason.`09`
  )

  "StatusReason" should {
    "convert to downstream value correctly" in {
      allStatusReason.foreach { statusReason =>
        val json = Json.toJson(statusReason)
        json shouldBe JsString(statusReason.downstreamValue)
      }

    }
    "read from JSON correctly" in {
      val json   = Json.parse("\"05\"")
      val result = Json.fromJson[StatusReason](json)
      result shouldBe JsSuccess(StatusReason.`05`)
    }
  }

}

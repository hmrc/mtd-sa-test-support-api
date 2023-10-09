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
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.Status
import utils.enums.EnumJsonSpecSupport

class StatusSpec extends UnitSpec with EnumJsonSpecSupport {

  implicit val writes: Writes[Status] = (obj: Status) => JsString(obj.downstreamValue)

  val allStatus: List[Status] = List(
    Status.`00`,
    Status.`01`,
    Status.`02`,
    Status.`03`,
    Status.`04`,
    Status.`05`,
    Status.`99`
  )

  "Status" should {
    "convert Status to downstreamValue" in {
      allStatus.foreach { statusReason =>
        val json = Json.toJson(statusReason)
        json shouldBe JsString(statusReason.downstreamValue)
      }
    }

    "read from JSON correctly" in {
      val json   = Json.parse("\"00\"")
      val result = Json.fromJson[Status](json)
      result shouldBe JsSuccess(Status.`00`)
    }
  }

}

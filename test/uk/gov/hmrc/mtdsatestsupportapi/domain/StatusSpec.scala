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

import play.api.libs.json.{JsError, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, JsonValidationError}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.Status
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.Status._

class StatusSpec extends UnitSpec {

  private val allStatuses: List[Status] = List(`00`, `01`, `02`, `03`, `04`, `05`, `99`)

  "Status" should {
    allStatuses.foreach { status =>
      s"read $status from JSON correctly" in {
        val json                     = JsString(status.toString)
        val result: JsResult[Status] = Json.fromJson[Status](json)
        result shouldBe JsSuccess(status)
      }

      s"write $status to downstream format correctly" in {
        val downstreamJson: JsValue = JsString(status.downstreamValue)
        Json.toJson(status) shouldBe downstreamJson
      }
    }
    "return a JsError" when {
      "reading an invalid Status" in {
        val json: JsValue            = JsString("021")
        val result: JsResult[Status] = Json.fromJson[Status](json)

        result shouldBe JsError(JsPath, JsonValidationError("error.expected.Status"))
      }
    }
  }

}

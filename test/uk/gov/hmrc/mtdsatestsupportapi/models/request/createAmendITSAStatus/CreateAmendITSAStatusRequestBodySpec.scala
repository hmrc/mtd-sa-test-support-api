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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus

import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}

class CreateAmendITSAStatusRequestBodySpec extends UnitSpec {

  private val itsaRequestBody = CreateAmendITSAStatusRequestBody(
    List(ITSAStatusDetail("2021-03-23T16:02:34.039Z", Status.`00`, StatusReason.`01`, None))
  )

  private val requestJson = Json.parse("""
      |{
      | "itsaStatusDetails": [
      |    {
      |     "submittedOn": "2021-03-23T16:02:34.039Z",
      |     "status": "00",
      |     "statusReason": "01"
      |     }
      |   ]
      |}
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      |{
      | "itsaStatusDetails": [
      |    {
      |     "submittedOn": "2021-03-23T16:02:34.039Z",
      |     "status": "No Status",
      |     "statusReason": "Sign up - no return available"
      |     }
      |   ]
      |}
      |""".stripMargin)

  "ITSAStatusRequestBody" should {
    "read from vendor Json" in {
      requestJson.as[CreateAmendITSAStatusRequestBody] shouldBe itsaRequestBody
    }

    "write to downstream Json" in {
      Json.toJson(itsaRequestBody) shouldBe downstreamJson
    }
  }

}
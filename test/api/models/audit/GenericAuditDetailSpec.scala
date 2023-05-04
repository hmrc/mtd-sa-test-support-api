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

package api.models.audit

import api.models.auth.UserDetails
import play.api.http.Status.OK
import play.api.libs.json.{Json, JsValue, OWrites}
import support.UnitSpec

class GenericAuditDetailSpec extends UnitSpec {

  "writes" must {
    "work" in {
      val userDetails = UserDetails(mtdId = "ignoredMtdId", userType = "Agent", agentReferenceNumber = Some("someARN"))

      Json.toJson(
        GenericAuditDetail(
          userDetails = userDetails,
          params = Params(nino = "someNino", businessId = "someBusinessId", request = Json.obj("requestField" -> "value")),
          correlationId = "someCorrelationId",
          response = AuditResponse(
            OK,
            Right(Some(Json.obj("responseField" -> "value")))
          )
        )) shouldBe Json.parse("""
          |{
          |   "versionNumber": "2.0",
          |   "userType": "Agent",
          |   "agentReferenceNumber": "someARN",
          |   "nino": "someNino",
          |   "businessId": "someBusinessId",
          |   "X-CorrelationId": "someCorrelationId",
          |   "request": {
          |     "requestField": "value"
          |   },
          |   "response": {
          |     "httpStatus": 200,
          |     "body": {
          |       "responseField": "value"
          |     }
          |   }
          |}""".stripMargin)
    }
  }

  case class Params(nino: String, businessId: String, request: JsValue)

  object Params {
    implicit val writes: OWrites[Params] = Json.writes
  }

}

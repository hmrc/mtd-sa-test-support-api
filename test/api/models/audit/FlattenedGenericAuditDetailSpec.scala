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
import api.models.errors.TaxYearFormatError
import mocks.MockAppConfig
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{Json, JsValue}
import support.UnitSpec

class FlattenedGenericAuditDetailSpec extends UnitSpec with MockAppConfig {

  val versionNumber: String                = "99.0"
  val nino: String                         = "XX751130C"
  val taxYear: String                      = "2021-22"
  val employmentId: String                 = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val userType: String                     = "Agent"
  val userDetails: UserDetails             = UserDetails("mtdId", userType, agentReferenceNumber)
  val correlationId: String                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "field": "value"
      |}
        """.stripMargin
  )

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
      |{
      |    "versionNumber": "$versionNumber",
      |    "userType": "$userType",
      |    "agentReferenceNumber": "${agentReferenceNumber.get}",
      |    "nino": "$nino",
      |    "taxYear": "$taxYear",
      |    "field": "value",
      |    "X-CorrelationId": "$correlationId",
      |    "response": "success",
      |    "httpStatusCode": $OK
      |}
    """.stripMargin
  )

  val auditDetailModelSuccess: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userType = userDetails.userType,
    agentReferenceNumber = agentReferenceNumber,
    params = Map("nino" -> nino, "taxYear" -> taxYear),
    request = Some(requestBodyJson),
    `X-CorrelationId` = correlationId,
    response = "success",
    httpStatusCode = OK,
    errorCodes = None,
    responseBody = None
  )

  val invalidTaxYearAuditDetailJson: JsValue = Json.parse(
    s"""
      |{
      |    "versionNumber": "$versionNumber",
      |    "userType": "$userType",
      |    "agentReferenceNumber": "${agentReferenceNumber.get}",
      |    "nino": "$nino",
      |    "taxYear" : "2021-2022",
      |    "field": "value",
      |    "X-CorrelationId": "$correlationId",
      |    "response": "error",
      |    "httpStatusCode": $BAD_REQUEST,
      |    "errorCodes": [
      |       "FORMAT_TAX_YEAR"
      |    ]
      |}
    """.stripMargin
  )

  val invalidTaxYearAuditDetailModel: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userType = userDetails.userType,
    agentReferenceNumber = agentReferenceNumber,
    params = Map("nino" -> nino, "taxYear" -> "2021-2022"),
    request = Some(requestBodyJson),
    `X-CorrelationId` = correlationId,
    response = "error",
    httpStatusCode = BAD_REQUEST,
    errorCodes = Some(List(TaxYearFormatError.code)),
    responseBody = None
  )

  "FlattenedGenericAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(auditDetailModelSuccess) shouldBe auditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(invalidTaxYearAuditDetailModel) shouldBe invalidTaxYearAuditDetailJson
      }
    }
  }

}

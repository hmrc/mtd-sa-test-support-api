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

package v1.createTestBusiness.fixture

import play.api.libs.json.*
import v1.createTestBusiness.model.request.*
import v1.createTestBusiness.model.response.CreateTestBusinessResponse

object CreateTestBusinessFixture {

  val latencyDetailsModel: LatencyDetails = LatencyDetails(
    latencyEndDate = "2025-04-05",
    taxYear1 = "2023-24",
    latencyIndicator1 = "A",
    taxYear2 = "2024-25",
    latencyIndicator2 = "Q"
  )

  val quarterlyTypeChoiceModel: QuarterlyTypeChoice = QuarterlyTypeChoice(
    quarterlyPeriodType = "standard",
    taxYearOfChoice = "2023-24"
  )

  val maxSelfEmploymentRequestBodyModel: CreateTestBusinessRequestBody = CreateTestBusinessRequestBody(
    typeOfBusiness = "self-employment",
    tradingType = Some("Plastering"),
    tradingName = Some("ABC Ltd"),
    firstAccountingPeriodStartDate = Some("2023-04-06"),
    firstAccountingPeriodEndDate = Some("2024-04-05"),
    latencyDetails = Some(latencyDetailsModel),
    quarterlyTypeChoice = Some(quarterlyTypeChoiceModel),
    accountingType = Some("ACCRUALS"),
    commencementDate = Some("2023-04-06"),
    cessationDate = Some("2024-04-05"),
    businessAddressLineOne = Some("Line 1 of address"),
    businessAddressLineTwo = Some("Line 2 of address"),
    businessAddressLineThree = Some("Line 3 of address"),
    businessAddressLineFour = Some("Line 4 of address"),
    businessAddressPostcode = Some("AB1 2CD"),
    businessAddressCountryCode = Some("FR")
  )

  def maxPropertyRequestBodyModel(typeOfBusiness: TypeOfBusiness): CreateTestBusinessRequestBody = CreateTestBusinessRequestBody(
    typeOfBusiness = typeOfBusiness.toString,
    tradingType = None,
    tradingName = None,
    firstAccountingPeriodStartDate = Some("2023-04-06"),
    firstAccountingPeriodEndDate = Some("2024-04-05"),
    latencyDetails = Some(latencyDetailsModel),
    quarterlyTypeChoice = Some(quarterlyTypeChoiceModel),
    accountingType = Some("ACCRUALS"),
    commencementDate = Some("2023-04-06"),
    cessationDate = Some("2024-04-05"),
    businessAddressLineOne = None,
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = None
  )

  def minPropertyRequestBodyModel(typeOfBusiness: TypeOfBusiness): CreateTestBusinessRequestBody = CreateTestBusinessRequestBody(
    typeOfBusiness = typeOfBusiness.toString,
    tradingType = None,
    tradingName = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    quarterlyTypeChoice = None,
    accountingType = None,
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = None,
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = None
  )

  val responseBodyModel: CreateTestBusinessResponse = CreateTestBusinessResponse(businessId = "X0IS12345678901")

  val downstreamLatencyDetailsJson: JsValue = Json.parse(
    """
      |{
      |  "latencyEndDate": "2025-04-05",
      |  "taxYear1": "2024",
      |  "latencyIndicator1": "A",
      |  "taxYear2": "2025",
      |  "latencyIndicator2": "Q"
      |}
    """.stripMargin
  )

  val downstreamQuarterlyTypeChoiceJson: JsValue = Json.parse(
    """
      |{
      |  "quarterReportingType": "STANDARD",
      |  "taxYearofElection": "2024"
      |}
    """.stripMargin
  )

  val downstreamSelfEmploymentRequestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "propertyIncomeFlag": false,
      |  "incomeSource": "Plastering",
      |  "tradingName": "ABC Ltd",
      |  "firstAccountingPeriodStartDate": "2023-04-06",
      |  "firstAccountingPeriodEndDate": "2024-04-05",
      |  "tradingSDate": "2023-04-06",
      |  "cessationDate": "2024-04-05",
      |  "latencyDetails": $downstreamLatencyDetailsJson,
      |  "quarterTypeElection": $downstreamQuarterlyTypeChoiceJson,
      |  "businessAddressDetails": {
      |    "addressLine1": "Line 1 of address",
      |    "addressLine2": "Line 2 of address",
      |    "addressLine3": "Line 3 of address",
      |    "addressLine4": "Line 4 of address",
      |    "postalCode": "AB1 2CD",
      |    "countryCode": "FR"
      |  },
      |  "selfEmployments": [
      |    {
      |      "accountingType": "ACCRUAL",
      |      "lateAccountingDate": {
      |        "eligible": false,
      |        "disapply": false
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  def downstreamPropertyRequestBodyJson(typeOfBusiness: String, incomeSourceType: Option[String] = None): JsValue = {
    val specifiedPropertyTypes: Set[String] = Set("ukProperty", "foreignProperty")

    val incomeSourceTypeJson: String = if (specifiedPropertyTypes.contains(typeOfBusiness)) {
      incomeSourceType.map(value => s""""incomeSourceType": "$value",""").getOrElse("")
    } else {
      ""
    }

    Json.parse(
      s"""
        |{
        |  "propertyIncomeFlag": true,
        |  $incomeSourceTypeJson
        |  "firstAccountingPeriodStartDate": "2023-04-06",
        |  "firstAccountingPeriodEndDate": "2024-04-05",
        |  "tradingSDate": "2023-04-06",
        |  "cessationDate": "2024-04-05",
        |  "latencyDetails": $downstreamLatencyDetailsJson,
        |  "quarterTypeElection": $downstreamQuarterlyTypeChoiceJson,
        |  "$typeOfBusiness": [
        |    {
        |      "accountingType": "ACCRUAL"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  val downstreamResponseBodyJson: JsValue = Json.parse(
    """
      |{
      |  "incomeSourceId": "X0IS12345678901"
      |}
    """.stripMargin
  )

  val mtdLatencyDetailsJson: JsValue = Json.parse(
    """
      |{
      |  "latencyEndDate": "2025-04-05",
      |  "taxYear1": "2023-24",
      |  "latencyIndicator1": "A",
      |  "taxYear2": "2024-25",
      |  "latencyIndicator2": "Q"
      |}
    """.stripMargin
  )

  val mtdQuarterlyTypeChoiceJson: JsValue = Json.parse(
    """
      |{
      |  "quarterlyPeriodType": "standard",
      |  "taxYearOfChoice": "2023-24"
      |}
    """.stripMargin
  )

  val mtdSelfEmploymentRequestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "typeOfBusiness": "self-employment",
      |  "tradingType": "Plastering",
      |  "tradingName": "ABC Ltd",
      |  "firstAccountingPeriodStartDate": "2023-04-06",
      |  "firstAccountingPeriodEndDate": "2024-04-05",
      |  "latencyDetails": $mtdLatencyDetailsJson,
      |  "quarterlyTypeChoice": $mtdQuarterlyTypeChoiceJson,
      |  "accountingType": "ACCRUALS",
      |  "commencementDate": "2023-04-06",
      |  "cessationDate": "2024-04-05",
      |  "businessAddressLineOne": "Line 1 of address",
      |  "businessAddressLineTwo": "Line 2 of address",
      |  "businessAddressLineThree": "Line 3 of address",
      |  "businessAddressLineFour": "Line 4 of address",
      |  "businessAddressPostcode": "AB1 2CD",
      |  "businessAddressCountryCode": "FR"
      |}
    """.stripMargin
  )

  def mtdPropertyRequestBodyJson(typeOfBusiness: TypeOfBusiness): JsValue = Json.parse(
    s"""
      |{
      |  "typeOfBusiness": "$typeOfBusiness",
      |  "firstAccountingPeriodStartDate": "2023-04-06",
      |  "firstAccountingPeriodEndDate": "2024-04-05",
      |  "latencyDetails": $mtdLatencyDetailsJson,
      |  "quarterlyTypeChoice": $mtdQuarterlyTypeChoiceJson,
      |  "accountingType": "ACCRUALS",
      |  "commencementDate": "2023-04-06",
      |  "cessationDate": "2024-04-05"
      |}
    """.stripMargin
  )

  val mtdResponseBodyJson: JsValue = Json.parse(
    """
      |{
      |  "businessId": "X0IS12345678901"
      |}
    """.stripMargin
  )

}

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

package uk.gov.hmrc.mtdsatestsupportapi.fixtures

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.{Business, TypeOfBusiness}
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createTestBusiness.CreateTestBusinessResponse

// Reusable JSON to support testing in various places outside of detailed JSON serialization testing
trait CreateTestBusinessFixtures {

  object MinimalCreateTestBusinessRequest {

    object SelfEmployment {

      val mtdBusinessJson: JsObject = Json
        .parse(
          """{
            |  "typeOfBusiness": "self-employment",
            |  "tradingName": "Self Employed Name",
            |  "businessAddressLineOne": "Line 1 of address",
            |  "businessAddressCountryCode": "FR"
            |}
            |""".stripMargin
        )
        .as[JsObject]

      val business: Business = Business(
        typeOfBusiness = TypeOfBusiness.`self-employment`,
        tradingName = Some("Self Employed Name"),
        firstAccountingPeriodStartDate = None,
        firstAccountingPeriodEndDate = None,
        latencyDetails = None,
        quarterlyTypeChoice = None,
        accountingType = None,
        commencementDate = None,
        cessationDate = None,
        businessAddressLineOne = Some("Line 1 of address"),
        businessAddressLineTwo = None,
        businessAddressLineThree = None,
        businessAddressLineFour = None,
        businessAddressPostcode = None,
        businessAddressCountryCode = Some("FR")
      )

      val downstreamBusinessJson: JsObject = Json
        .parse(
          """{
            |  "propertyIncome": false,
            |  "tradingName": "Self Employed Name",
            |  "businessAddressDetails": {
            |    "addressLine1": "Line 1 of address",
            |    "countryCode": "FR"
            |  }
            |}""".stripMargin
        )
        .as[JsObject]

    }

    object UkProperty {

      val mtdBusinessJson: JsObject = Json
        .parse(
          """{
            |  "typeOfBusiness": "uk-property"
            |}
            |""".stripMargin
        )
        .as[JsObject]

      val business: Business = Business(
        typeOfBusiness = TypeOfBusiness.`uk-property`,
        tradingName = None,
        firstAccountingPeriodStartDate = None,
        firstAccountingPeriodEndDate = None,
        quarterlyTypeChoice = None,
        latencyDetails = None,
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

      val downstreamBusinessJson: JsObject = Json
        .parse(
          """{
            |  "propertyIncome": true
            |}""".stripMargin
        )
        .as[JsObject]

    }

  }

  object ExampleCreateTestBusinessResponse {
    val businessId                           = "someBusinessId"
    val response: CreateTestBusinessResponse = CreateTestBusinessResponse(businessId)
    val downstreamResponseJson: JsObject     = Json.obj("incomeSourceId" -> businessId)
  }

}

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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec
import TypeOfBusiness._
import api.models.domain.TaxYear
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateBusinessFixtures

class BusinessSpec extends UnitSpec with CreateBusinessFixtures {

  private def business(typeOfBusiness: TypeOfBusiness = `self-employment`) = Business(
    typeOfBusiness = typeOfBusiness,
    tradingName = Some("Abc Ltd"),
    firstAccountingPeriodStartDate = Some("2002-02-02"),
    firstAccountingPeriodEndDate = Some("2012-12-12"),
    latencyDetails = Some(
      LatencyDetails(
        latencyEndDate = "2020-01-01",
        taxYear1 = TaxYear.fromMtd("2020-21"),
        latencyIndicator1 = LatencyIndicator.A,
        taxYear2 = TaxYear.fromMtd("2021-22"),
        latencyIndicator2 = LatencyIndicator.Q
      )),
    accountingType = Some(AccountingType.CASH),
    commencementDate = Some("2000-01-01"),
    cessationDate = Some("2030-01-01"),
    businessAddressLineOne = Some("L1"),
    businessAddressLineTwo = Some("L2"),
    businessAddressLineThree = Some("L3"),
    businessAddressLineFour = Some("L4"),
    businessAddressPostcode = Some("PostCode"),
    businessAddressCountryCode = Some("UK")
  )

  "Business" must {
    "deserialized from the API JSON" when {
      "all fields present" must {
        "work" in {
          val mtdJson = Json.parse("""{
              |  "typeOfBusiness": "self-employment",
              |  "tradingName": "Abc Ltd",
              |  "firstAccountingPeriodStartDate": "2002-02-02",
              |  "firstAccountingPeriodEndDate": "2012-12-12",
              |  "latencyDetails": {
              |    "latencyEndDate": "2020-01-01",
              |    "taxYear1": "2020-21",
              |    "latencyIndicator1": "A",
              |    "taxYear2": "2021-22",
              |    "latencyIndicator2": "Q"
              |  },
              |  "accountingType": "CASH",
              |  "commencementDate": "2000-01-01",
              |  "cessationDate": "2030-01-01",
              |  "businessAddressLineOne": "L1",
              |  "businessAddressLineTwo": "L2",
              |  "businessAddressLineThree": "L3",
              |  "businessAddressLineFour": "L4",
              |  "businessAddressPostcode": "PostCode",
              |  "businessAddressCountryCode": "UK"
              |}
              |""".stripMargin)

          mtdJson.as[Business] shouldBe business()
        }
      }

      "only mandatory fields are present" must {
        "work" in {
          MinimalCreateBusinessRequest.mtdBusinessJson.as[Business] shouldBe
            MinimalCreateBusinessRequest.business
        }
      }
    }

    "serialized to downstream JSON" when {

      def testSerializeToBackend(typeOfBusiness: TypeOfBusiness, expectedIncomeSourceType: Option[String], expectedPropertyIncome: Boolean): Unit = {
        s"typeOfBusiness is $typeOfBusiness" must {
          "work and set propertyIncome and incomeSourceType correctly" in {
            Json.toJson(business(typeOfBusiness)) shouldBe {
              Json
                .parse(s"""{
                     |  "tradingName": "Abc Ltd",
                     |  "propertyIncome": $expectedPropertyIncome,
                     |  "firstAccountingPeriodStartDate": "2002-02-02",
                     |  "firstAccountingPeriodEndDate": "2012-12-12",
                     |  "latencyDetails": {
                     |    "latencyEndDate": "2020-01-01",
                     |    "taxYear1": "2021",
                     |    "latencyIndicator1": "A",
                     |    "taxYear2": "2022",
                     |    "latencyIndicator2": "Q"
                     |  },
                     |  "cashOrAccruals": false,
                     |  "tradingStartDate": "2000-01-01",
                     |  "cessationDate": "2030-01-01",
                     |  "businessAddressDetails": {
                     |    "addressLine1": "L1",
                     |    "addressLine2": "L2",
                     |    "addressLine3": "L3",
                     |    "addressLine4": "L4",
                     |    "postalCode": "PostCode",
                     |    "countryCode": "UK"
                     |  }
                     |}""".stripMargin)
                .as[JsObject] ++
                expectedIncomeSourceType.map(x => Json.obj("incomeSourceType" -> x)).getOrElse(JsObject.empty)
            }
          }
        }
      }

      testSerializeToBackend(`foreign-property`, expectedIncomeSourceType = Some("foreign-property"), expectedPropertyIncome = true)
      testSerializeToBackend(`uk-property`, expectedIncomeSourceType = Some("uk-property"), expectedPropertyIncome = true)
      testSerializeToBackend(`property-unspecified`, expectedIncomeSourceType = None, expectedPropertyIncome = true)
      testSerializeToBackend(`self-employment`, expectedIncomeSourceType = None, expectedPropertyIncome = false)

    }

  }

}

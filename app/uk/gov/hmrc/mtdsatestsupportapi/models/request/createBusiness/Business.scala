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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.enums.Enums

case class Business(
    typeOfBusiness: TypeOfBusiness,
    tradingName: Option[String],
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    accountingType: Option[AccountingType],
    commencementDate: Option[String],
    cessationDate: Option[String],
    businessAddressLineOne: Option[String],
    businessAddressLineTwo: Option[String],
    businessAddressLineThree: Option[String],
    businessAddressLineFour: Option[String],
    businessAddressPostcode: Option[String],
    businessAddressCountryCode: Option[String]
)

object Business {
  implicit val reads: Reads[Business] = Json.reads

  private implicit val typeOfBusinessWrites: Writes[TypeOfBusiness] = {
    import TypeOfBusiness._

    val defaultWrites: Writes[TypeOfBusiness] = Enums.writes[TypeOfBusiness]

    (typeOfBusiness: TypeOfBusiness) => {
      val propertyIncomeJson = Json.obj("propertyIncome" -> isProperty(typeOfBusiness))

      typeOfBusiness match {
        case `uk-property` | `foreign-property` => propertyIncomeJson + ("incomeSourceType" -> Json.toJson(typeOfBusiness)(defaultWrites))
        case _                                  => propertyIncomeJson
      }
    }
  }

  implicit val writes: Writes[Business] = (
    __.write[TypeOfBusiness] and
      (__ \ "tradingName").writeNullable[String] and
      (__ \ "accountingPeriodStartDate").write[String] and
      (__ \ "accountingPeriodEndDate").write[String] and
      (__ \ "firstAccountingPeriodStartDate").writeNullable[String] and
      (__ \ "firstAccountingPeriodEndDate").writeNullable[String] and
      (__ \ "latencyDetails").writeNullable[LatencyDetails] and
      (__ \ "cashOrAccruals").writeNullable[AccountingType] and
      (__ \ "tradingStartDate").writeNullable[String] and
      (__ \ "cessationDate").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "addressLine1").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "addressLine2").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "addressLine3").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "addressLine4").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "postalCode").writeNullable[String] and
      (__ \ "businessAddressDetails" \ "countryCode").writeNullable[String]
  )(unlift(Business.unapply))

}

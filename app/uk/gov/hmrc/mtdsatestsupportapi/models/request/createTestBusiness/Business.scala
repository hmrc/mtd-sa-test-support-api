/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness

import play.api.libs.json._

import java.time.LocalDate

case class Business(typeOfBusiness: TypeOfBusiness,
                    tradingName: Option[String],
                    firstAccountingPeriodStartDate: Option[LocalDate],
                    firstAccountingPeriodEndDate: Option[LocalDate],
                    latencyDetails: Option[LatencyDetails],
                    quarterlyTypeChoice: Option[QuarterlyTypeChoice],
                    accountingType: Option[AccountingType],
                    commencementDate: Option[LocalDate],
                    cessationDate: Option[LocalDate],
                    businessAddressLineOne: Option[String],
                    businessAddressLineTwo: Option[String],
                    businessAddressLineThree: Option[String],
                    businessAddressLineFour: Option[String],
                    businessAddressPostcode: Option[String],
                    businessAddressCountryCode: Option[String]
                   ) {

  def hasAnyBusinessAddressDetails: Boolean = {
    businessAddressLineOne.isDefined ||
      businessAddressLineTwo.isDefined ||
      businessAddressLineThree.isDefined ||
      businessAddressLineFour.isDefined ||
      businessAddressPostcode.isDefined ||
      businessAddressCountryCode.isDefined
  }

}

object Business {
  implicit val reads: Reads[Business] = Json.reads

  private implicit val typeOfBusinessWrites: OWrites[TypeOfBusiness] = {
    import TypeOfBusiness._

    (typeOfBusiness: TypeOfBusiness) => {
      val propertyIncomeJson = Json.obj("propertyIncomeFlag" -> typeOfBusiness.isProperty)

      typeOfBusiness match {
        case `uk-property` => propertyIncomeJson + ("incomeSourceType" -> JsString("02"))
        case `foreign-property` => propertyIncomeJson + ("incomeSourceType" -> JsString("03"))
        case _ => propertyIncomeJson
      }
    }
  }

  implicit val writes: OWrites[Business] = { business =>
    def trimEmpty(jsonObj: JsObject): JsObject = JsObject(
      jsonObj.fields.filter {
        case (_, JsNull) => false
        case (_, value: JsObject) => value.fields.nonEmpty
        case _ => true
      }
    )

    val accountingTypeJson: JsObject = {
      val accountingType: AccountingType = business.accountingType.getOrElse(AccountingType.CASH)

      val typeOfBusiness: String = business.typeOfBusiness match {
        case TypeOfBusiness.`self-employment` => "selfEmployments"
        case TypeOfBusiness.`foreign-property` => "foreignProperty"
        case _ => "ukProperty"
      }

      Json.obj(typeOfBusiness -> Json.arr(Json.obj("accountingType" -> Json.toJson(accountingType))))
    }

    val businessJson: JsObject = trimEmpty(
      Json.obj(
        "tradingName" -> business.tradingName,
        "firstAccountingPeriodStartDate" -> business.firstAccountingPeriodStartDate,
        "firstAccountingPeriodEndDate" -> business.firstAccountingPeriodEndDate,
        "latencyDetails" -> business.latencyDetails,
        "quarterTypeElection" -> business.quarterlyTypeChoice,
        "tradingSDate" -> business.commencementDate,
        "cessationDate" -> business.cessationDate,
        "businessAddressDetails" -> trimEmpty(
          Json.obj(
            "addressLine1" -> business.businessAddressLineOne,
            "addressLine2" -> business.businessAddressLineTwo,
            "addressLine3" -> business.businessAddressLineThree,
            "addressLine4" -> business.businessAddressLineFour,
            "postalCode" -> business.businessAddressPostcode,
            "countryCode" -> business.businessAddressCountryCode
          )
        )
      )
    ) ++ Json.toJsObject(business.typeOfBusiness) ++ accountingTypeJson

    businessJson
  }

}

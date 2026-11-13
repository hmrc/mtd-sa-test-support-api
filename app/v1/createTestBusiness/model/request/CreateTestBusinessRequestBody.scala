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

package v1.createTestBusiness.model.request

import play.api.libs.json.*

case class CreateTestBusinessRequestBody(typeOfBusiness: String,
                                         tradingType: Option[String],
                                         tradingName: Option[String],
                                         firstAccountingPeriodStartDate: Option[String],
                                         firstAccountingPeriodEndDate: Option[String],
                                         latencyDetails: Option[LatencyDetails],
                                         quarterlyTypeChoice: Option[QuarterlyTypeChoice],
                                         accountingType: Option[String],
                                         commencementDate: Option[String],
                                         cessationDate: Option[String],
                                         businessAddressLineOne: Option[String],
                                         businessAddressLineTwo: Option[String],
                                         businessAddressLineThree: Option[String],
                                         businessAddressLineFour: Option[String],
                                         businessAddressPostcode: Option[String],
                                         businessAddressCountryCode: Option[String]) {

  def hasAnyBusinessAddressDetails: Boolean = {
    businessAddressLineOne.isDefined ||
    businessAddressLineTwo.isDefined ||
    businessAddressLineThree.isDefined ||
    businessAddressLineFour.isDefined ||
    businessAddressPostcode.isDefined ||
    businessAddressCountryCode.isDefined
  }

}

object CreateTestBusinessRequestBody {
  private def parsedTypeOfBusiness(typeOfBusiness: String): TypeOfBusiness = TypeOfBusiness.parser(typeOfBusiness)

  implicit val reads: Reads[CreateTestBusinessRequestBody] = Json.reads[CreateTestBusinessRequestBody]

  private def typeOfBusinessDetailsJson(typeOfBusiness: String): JsObject = {
    import TypeOfBusiness.*

    val parsedType: TypeOfBusiness   = parsedTypeOfBusiness(typeOfBusiness)
    val propertyIncomeJson: JsObject = Json.obj("propertyIncomeFlag" -> parsedType.isProperty)

    parsedType match {
      case `uk-property`      => propertyIncomeJson + ("incomeSourceType" -> JsString("02"))
      case `foreign-property` => propertyIncomeJson + ("incomeSourceType" -> JsString("03"))
      case _                  => propertyIncomeJson
    }
  }

  implicit val writes: OWrites[CreateTestBusinessRequestBody] = { business =>
    def trimEmpty(jsonObj: JsObject): JsObject = JsObject(
      jsonObj.fields.filter {
        case (_, JsNull)          => false
        case (_, value: JsObject) => value.fields.nonEmpty
        case _                    => true
      }
    )

    val baseJson: JsObject = trimEmpty(
      Json.obj(
        "incomeSource"                   -> business.tradingType,
        "tradingName"                    -> business.tradingName,
        "firstAccountingPeriodStartDate" -> business.firstAccountingPeriodStartDate,
        "firstAccountingPeriodEndDate"   -> business.firstAccountingPeriodEndDate,
        "latencyDetails"                 -> business.latencyDetails,
        "quarterTypeElection"            -> business.quarterlyTypeChoice,
        "tradingSDate"                   -> business.commencementDate,
        "cessationDate"                  -> business.cessationDate,
        "businessAddressDetails" -> trimEmpty(
          Json.obj(
            "addressLine1" -> business.businessAddressLineOne,
            "addressLine2" -> business.businessAddressLineTwo,
            "addressLine3" -> business.businessAddressLineThree,
            "addressLine4" -> business.businessAddressLineFour,
            "postalCode"   -> business.businessAddressPostcode,
            "countryCode"  -> business.businessAddressCountryCode
          )
        )
      )
    ) ++ typeOfBusinessDetailsJson(business.typeOfBusiness)

    val accountingTypeJson: JsObject = Json.obj(
      "accountingType" -> business.accountingType.fold(AccountingType.CASH.toDownstream)(AccountingType.parser(_).toDownstream)
    )

    val typeOfBusinessJson: JsObject = if (parsedTypeOfBusiness(business.typeOfBusiness).isSelfEmployment) {
      val lateAccountingDateRuleJson: JsObject = Json.obj(
        "lateAccountingDate" -> Json.obj("eligible" -> false, "disapply" -> false)
      )

      Json.obj("selfEmployments" -> Json.arr(accountingTypeJson ++ lateAccountingDateRuleJson))
    } else {
      val typeOfBusiness: String = parsedTypeOfBusiness(business.typeOfBusiness) match {
        case TypeOfBusiness.`foreign-property` => "foreignProperty"
        case _                                 => "ukProperty"
      }

      Json.obj(typeOfBusiness -> Json.arr(accountingTypeJson))
    }

    baseJson ++ typeOfBusinessJson
  }

}

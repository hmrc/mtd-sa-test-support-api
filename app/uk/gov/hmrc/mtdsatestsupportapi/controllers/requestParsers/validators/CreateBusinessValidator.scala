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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors._
import play.api.libs.json.{JsDefined, JsLookupResult, JsValue}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations._
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness.{Business, CreateBusinessRawData}

import java.time.{Clock, LocalDate}
import javax.inject.{Inject, Singleton}
import scala.math.Ordered.orderingToOrdered
import scala.util.{Failure, Success, Try}

@Singleton
class CreateBusinessValidator @Inject() (clock: Clock) extends Validator[CreateBusinessRawData] {

  override def validate(data: CreateBusinessRawData): List[MtdError] =
    (for {
      _ <- parameterFormatValidation(data)
      _ <- bodyEnumValidation(data.body)
      _ <- bodyFormatValidation(data)
      _ <- bodyFieldValidation(data)
    } yield ()).swap.getOrElse(Nil)

  private def parameterFormatValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val errors = NinoValidation.validate(data.nino)

    errorsResult(errors)
  }

  private def bodyEnumValidation(body: JsValue): Either[List[MtdError], Unit] = {
    val errors = validateAccountingType(body) ++ validateTypeOfBusiness(body) ++
      validateLatencyIndicator(body, "/latencyDetails/latencyIndicator1") ++
      validateLatencyIndicator(body, "/latencyDetails/latencyIndicator2") ++
      validateTaxYear(body, "/latencyDetails/taxYear1") ++
      validateTaxYear(body, "/latencyDetails/taxYear2")

    errorsResult(errors)
  }

  private def bodyFormatValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val errors = JsonFormatValidation.validate[Business](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => schemaErrors
    }

    errorsResult(errors)
  }

  private def bodyFieldValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val business = data.body.as[Business]

    val errors =
      business.businessAddressCountryCode.map(CountryCodeValidation.validate(_)).getOrElse(Nil) ++
        validatePostcode(business) ++
        validateMissingPostcode(business) ++
        business.firstAccountingPeriodStartDate.map(validateDate(_, "/firstAccountingPeriodStartDate")).getOrElse(Nil) ++
        business.firstAccountingPeriodEndDate.map(validateDate(_, "/firstAccountingPeriodEndDate")).getOrElse(Nil) ++
        business.latencyDetails.map(ld => validateDate(ld.latencyEndDate, "/latencyDetails/latencyEndDate")).getOrElse(Nil) ++
        business.commencementDate.map(validateCommencementDate).getOrElse(Nil) ++
        business.cessationDate.map(validateDate(_, "/cessationDate")).getOrElse(Nil)

    errorsResult(errors)
  }

  private def validateDate(value: String, path: String): Seq[MtdError] =
    DateValidation.validate(value, DateFormatError.withExtraPath(path))

  private def validateAccountingType(json: JsValue): Seq[MtdError] =
    (json \ "accountingType").asOpt[String].map(AccountingTypeValidation.validate(_)).getOrElse(Nil)

  private def validateTypeOfBusiness(json: JsValue): Seq[MtdError] =
    (json \ "typeOfBusiness").asOpt[String].map(TypeOfBusinessValidation.validate(_)).getOrElse(Nil)

  private def validateLatencyIndicator(json: JsValue, path: String): Seq[MtdError] =
    jsLookupFrom(json, path).asOpt[String].map(LatencyIndicatorValidation.validate(_, LatencyIndicatorFormatError.withExtraPath(path))).getOrElse(Nil)

  private def validateTaxYear(json: JsValue, path: String): Seq[MtdError] =
    jsLookupFrom(json, path).asOpt[String].map(TaxYearValidation.validate(_, path)).getOrElse(Nil)

  private def jsLookupFrom(json: JsValue, path: String) =
    path.split("/").filter(_.nonEmpty).foldLeft[JsLookupResult](JsDefined(json))(_ \ _)

  private def validatePostcode(business: Business): Seq[MtdError] =
    business.businessAddressPostcode.map(PostcodeValidation.validate(_)).getOrElse(Nil)

  private def validateMissingPostcode(business: Business): List[MtdError] = {
    (business.businessAddressPostcode, business.businessAddressCountryCode) match {
      case (None, Some("GB")) => List(MissingPostcodeError)
      case _                  => NoValidationErrors
    }
  }

  private def validateCommencementDate(commencementDate: String): List[MtdError] =
    Try(LocalDate.parse(commencementDate, dateFormat)) match {
      case Success(date) =>
        val today = LocalDate.now(clock)
        if (date < today) Nil
        else List(RuleCommencementDateNotSupported)

      case Failure(_) =>
        List(DateFormatError.withExtraPath("/commencementDate"))
    }

}

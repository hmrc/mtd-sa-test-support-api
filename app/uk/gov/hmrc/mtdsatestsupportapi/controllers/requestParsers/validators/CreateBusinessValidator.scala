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

@Singleton
class CreateBusinessValidator @Inject() (clock: Clock) extends Validator[CreateBusinessRawData] {

  override def validate(data: CreateBusinessRawData): List[MtdError] =
    (for {
      _ <- parameterFormatValidation(data)
      _ <- fieldFormatValidation(data.body)
      _ <- bodyFormatValidation(data)
      _ <- bodyRuleValidation(data)
    } yield ()).swap.getOrElse(Nil)

  private def parameterFormatValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val errors = NinoValidation.validate(data.nino)

    errorsResult(errors)
  }

  private def fieldFormatValidation(body: JsValue): Either[List[MtdError], Unit] = {
    def validateField(path: String)(validation: String => Seq[MtdError]) = {
      val jsLookupResult = path.split("/").filter(_.nonEmpty).foldLeft[JsLookupResult](JsDefined(body))(_ \ _)
      jsLookupResult.asOpt[String].map(validation).getOrElse(Nil)
    }

    def validateDate(path: String) = validateField(path)(DateValidation.validate(_, DateFormatError.withExtraPath(path)))

    def validateLatencyIndicator(path: String) =
      validateField(path)(LatencyIndicatorValidation.validate(_, LatencyIndicatorFormatError.withExtraPath(path)))

    def validateTaxYear(path: String) = validateField(path)(TaxYearValidation.validate(_, path))

    val errors = validateField("/accountingType")(AccountingTypeValidation.validate(_)) ++
      validateField("/typeOfBusiness")(TypeOfBusinessValidation.validate(_)) ++
      validateLatencyIndicator("/latencyDetails/latencyIndicator1") ++
      validateLatencyIndicator("/latencyDetails/latencyIndicator2") ++
      validateTaxYear("/latencyDetails/taxYear1") ++
      validateTaxYear("/latencyDetails/taxYear2") ++
      validateField("/businessAddressCountryCode")(CountryCodeValidation.validate(_)) ++
      validateField("/businessAddressPostcode")(PostcodeValidation.validate(_)) ++
      validateDate("/firstAccountingPeriodStartDate") ++
      validateDate("/firstAccountingPeriodEndDate") ++
      validateDate("/latencyDetails/latencyEndDate") ++
      validateDate("/commencementDate") ++
      validateDate("/cessationDate")

    errorsResult(errors)
  }

  private def bodyFormatValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val errors = JsonFormatValidation.validate[Business](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => schemaErrors
    }

    errorsResult(errors)
  }

  private def bodyRuleValidation(data: CreateBusinessRawData): Either[List[MtdError], Unit] = {
    val business = data.body.as[Business]

    val errors =
      validateMissingPostcode(business) ++
        business.commencementDate.map(validateCommencementDate).getOrElse(Nil)

    errorsResult(errors)
  }

  private def validateMissingPostcode(business: Business): List[MtdError] = {
    (business.businessAddressPostcode, business.businessAddressCountryCode) match {
      case (None, Some("GB")) => List(MissingPostcodeError)
      case _                  => NoValidationErrors
    }
  }

  private def validateCommencementDate(commencementDate: String): List[MtdError] = {
    // Safe as we will already have checked that it parses successfully
    val date  = LocalDate.parse(commencementDate, dateFormat)
    val today = LocalDate.now(clock)

    if (date < today) Nil else List(RuleCommencementDateNotSupported)
  }

}

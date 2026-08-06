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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.*
import api.models.errors.*
import play.api.libs.json.{JsDefined, JsLookupResult, JsValue}
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations.*
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.{Business, CreateTestBusinessRawData}

import java.time.{Clock, LocalDate}
import javax.inject.{Inject, Singleton}
import scala.math.Ordered.orderingToOrdered

@Singleton
class CreateTestBusinessValidator @Inject() (clock: Clock) extends Validator[CreateTestBusinessRawData] {

  override def validate(data: CreateTestBusinessRawData): List[MtdError] =
    (for {
      _ <- parameterFormatValidation(data)
      _ <- fieldFormatValidation(data.body)
      _ <- bodyFormatValidation(data)
      _ <- bodyRuleValidation(data)
    } yield ()).swap.getOrElse(Nil)

  private def parameterFormatValidation(data: CreateTestBusinessRawData): Either[List[MtdError], Unit] = {
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

    def validateQuarterlyPeriodType(path: String) =
      validateField(path)(QuarterlyPeriodTypeValidation.validate(_, QuarterlyPeriodTypeFormatError))

    def validateTaxYear(path: String) = validateField(path)(TaxYearValidation.validate(_, path))

    def validateStringField(path: String, maxLength: Int) = validateField(path)(StringFieldValidation.validate(maxLength, path))

    val errors = Seq(
      validateField("/accountingType")(AccountingTypeValidation.validate(_)),
      validateField("/typeOfBusiness")(TypeOfBusinessValidation.validate(_)),
      validateField("/businessAddressCountryCode")(CountryCodeValidation.validate(_)),
      validateField("/businessAddressPostcode")(PostcodeValidation.validate(_)),
      validateStringField("/tradingType", 35),
      validateStringField("/tradingName", 105),
      validateStringField("/businessAddressLineOne", 35),
      validateStringField("/businessAddressLineTwo", 35),
      validateStringField("/businessAddressLineThree", 35),
      validateStringField("/businessAddressLineFour", 35),
      validateLatencyIndicator("/latencyDetails/latencyIndicator1"),
      validateLatencyIndicator("/latencyDetails/latencyIndicator2"),
      validateTaxYear("/latencyDetails/taxYear1"),
      validateTaxYear("/latencyDetails/taxYear2"),
      validateTaxYear("/quarterlyTypeChoice/taxYearOfChoice"),
      validateQuarterlyPeriodType("/quarterlyTypeChoice/quarterlyPeriodType"),
      validateDate("/firstAccountingPeriodStartDate"),
      validateDate("/firstAccountingPeriodEndDate"),
      validateDate("/latencyDetails/latencyEndDate"),
      validateDate("/commencementDate"),
      validateDate("/cessationDate")
    ).flatten

    errorsResult(errors)
  }

  private def bodyFormatValidation(data: CreateTestBusinessRawData): Either[List[MtdError], Unit] = {
    val errors = JsonFormatValidation.validate[Business](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => schemaErrors
    }

    errorsResult(errors)
  }

  private def bodyRuleValidation(data: CreateTestBusinessRawData): Either[List[MtdError], Unit] = {
    val business = data.body.as[Business]

    val errors = Seq(
      validateMissingPostcode(business),
      validateAccountingPeriod(business),
      validateBusinessAddress(business),
      validateTradingType(business),
      validateTradingName(business),
      business.commencementDate.map(validateCommencementDate).getOrElse(Nil)
    ).flatten

    errorsResult(errors)
  }

  private def validateMissingPostcode(business: Business): Seq[MtdError] = {
    if (requiresBusinessPostcode(business) && business.businessAddressPostcode.isEmpty) {
      Seq(MissingPostcodeError)
    } else {
      Nil
    }
  }

  private def validateAccountingPeriod(business: Business): Seq[MtdError] = {
    (business.firstAccountingPeriodStartDate, business.firstAccountingPeriodEndDate) match {
      case (Some(start), Some(end)) => TaxYearAlignmentDateRangeValidation.validate(start, end, RuleFirstAccountingDateRangeInvalidError)
      case (None, Some(_))          => Seq(MissingFirstAccountingPeriodStartDateError)
      case (Some(_), None)          => Seq(MissingFirstAccountingPeriodEndDateError)
      case _                        => Nil
    }
  }

  private def validateCommencementDate(date: LocalDate): List[MtdError] = {
    val today = LocalDate.now(clock)

    if (date < today) Nil else List(RuleCommencementDateNotSupportedError)
  }

  private def validateTradingField(business: Business, field: Option[String], missingError: MtdError, unexpectedError: MtdError): Seq[MtdError] = {
    if (isProperty(business) && field.nonEmpty) {
      Seq(unexpectedError)
    } else if (isSelfEmployment(business) && field.isEmpty) {
      Seq(missingError)
    } else {
      Nil
    }
  }

  private def validateTradingType(business: Business): Seq[MtdError] = validateTradingField(
    business = business,
    field = business.tradingType,
    missingError = RuleMissingTradingTypeError,
    unexpectedError = RuleUnexpectedTradingTypeError
  )

  private def validateTradingName(business: Business): Seq[MtdError] = validateTradingField(
    business = business,
    field = business.tradingName,
    missingError = RuleMissingTradingNameError,
    unexpectedError = RuleUnexpectedTradingNameError
  )

  private def validateBusinessAddress(business: Business): Seq[MtdError] = {
    def haveSufficientBusinessAddress = business.businessAddressLineOne.nonEmpty && business.businessAddressCountryCode.nonEmpty

    if (isProperty(business) && business.hasAnyBusinessAddressDetails) {
      Seq(RuleUnexpectedBusinessAddressError)
    } else if (isSelfEmployment(business) && !haveSufficientBusinessAddress) {
      Seq(RuleMissingBusinessAddressError)
    } else {
      Nil
    }
  }

  private def requiresBusinessPostcode(business: Business): Boolean = {
    isSelfEmployment(business) &&
    business.businessAddressCountryCode.contains("GB")
  }

  private def isSelfEmployment(business: Business): Boolean = business.typeOfBusiness.isSelfEmployment

  private def isProperty(business: Business): Boolean = business.typeOfBusiness.isProperty

}

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

package v1.createTestBusiness

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.*
import api.models.domain.TaxYear
import api.models.errors.*
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.catsSyntaxTuple2Semigroupal
import v1.createTestBusiness.model.request.*

import java.time.{LocalDate, ZoneOffset}
import scala.util.matching.Regex

object CreateTestBusinessRulesValidator extends RulesValidator[CreateTestBusinessRequestData] {

  private val commonStringRegex: Regex = "^.{1,35}$".r
  private val tradingNameRegex: Regex  = "^.{1,105}$".r
  private val postcodeRegex: Regex     = "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$".r

  override def validateBusinessRules(parsed: CreateTestBusinessRequestData): Validated[Seq[MtdError], CreateTestBusinessRequestData] = {
    val body: CreateTestBusinessRequestBody = parsed.body

    combine(
      validateTypeOfBusiness(body),
      validateTradingType(body),
      validateTradingName(body),
      validateAccountingPeriod(body),
      validateLatencyDetails(body),
      validateQuarterlyTypeChoice(body),
      validateAccountingType(body),
      validateCommencementDate(body),
      validateCessationDate(body),
      validateBusinessAddress(body)
    ).onSuccess(parsed)
  }

  private def validateTypeOfBusiness(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    resolveEnum(TypeOfBusiness.parser, TypeOfBusinessFormatError.withPath("/typeOfBusiness"))(body.typeOfBusiness).toUnit

  private def validateTradingType(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    combine(
      ResolveStringPattern(body.tradingType, commonStringRegex, StringFormatError.withPath("/tradingType")),
      validateTradingFieldRule(body, body.tradingType, RuleUnexpectedTradingTypeError, RuleMissingTradingTypeError)
    )

  private def validateTradingName(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    combine(
      ResolveStringPattern(body.tradingName, tradingNameRegex, StringFormatError.withPath("/tradingName")),
      validateTradingFieldRule(body, body.tradingName, RuleUnexpectedTradingNameError, RuleMissingTradingNameError)
    )

  private def validateTradingFieldRule(body: CreateTestBusinessRequestBody,
                                       field: Option[String],
                                       unexpectedError: MtdError,
                                       missingError: MtdError): Validated[Seq[MtdError], Unit] = {
    if (isProperty(body) && field.nonEmpty) {
      Invalid(List(unexpectedError))
    } else if (isSelfEmployment(body) && field.isEmpty) {
      Invalid(List(missingError))
    } else {
      valid
    }
  }

  private def validateAccountingPeriod(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    (
      ResolveIsoDate(
        body.firstAccountingPeriodStartDate,
        DateFormatError.withPath("/firstAccountingPeriodStartDate")
      ),
      ResolveIsoDate(
        body.firstAccountingPeriodEndDate,
        DateFormatError.withPath("/firstAccountingPeriodEndDate")
      )
    ).tupled.andThen { (startDate, endDate) =>
      validateAccountingPeriodRule(startDate, endDate)
    }

  private def validateAccountingPeriodRule(firstAccountingPeriodStartDate: Option[LocalDate],
                                           firstAccountingPeriodEndDate: Option[LocalDate]): Validated[Seq[MtdError], Unit] =
    (firstAccountingPeriodStartDate, firstAccountingPeriodEndDate) match {
      case (Some(start), Some(end)) =>
        val taxYear: TaxYear = TaxYear.containing(start)
        Validated.cond(
          taxYear.startDate == start && taxYear.endDate == end,
          (),
          List(RuleFirstAccountingDateRangeInvalidError)
        )

      case (None, Some(_)) => Invalid(List(MissingFirstAccountingPeriodStartDateError))

      case (Some(_), None) => Invalid(List(MissingFirstAccountingPeriodEndDateError))

      case _ => valid
    }

  private def validateLatencyDetails(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    combine(
      ResolveIsoDate(
        body.latencyDetails.map(_.latencyEndDate),
        DateFormatError.withPath("/latencyDetails/latencyEndDate")
      ),
      ResolveTaxYear
        .resolverWithCustomErrors(
          TaxYearFormatError.withPath("/latencyDetails/taxYear1"),
          RuleTaxYearRangeInvalidError.withPath("/latencyDetails/taxYear1")
        )
        .resolveOptionally(body.latencyDetails.map(_.taxYear1)),
      resolveEnum(
        LatencyIndicator.parser,
        LatencyIndicatorFormatError.withPath("/latencyDetails/latencyIndicator1")
      ).resolveOptionally(body.latencyDetails.map(_.latencyIndicator1)),
      ResolveTaxYear
        .resolverWithCustomErrors(
          TaxYearFormatError.withPath("/latencyDetails/taxYear2"),
          RuleTaxYearRangeInvalidError.withPath("/latencyDetails/taxYear2")
        )
        .resolveOptionally(body.latencyDetails.map(_.taxYear2)),
      resolveEnum(
        LatencyIndicator.parser,
        LatencyIndicatorFormatError.withPath("/latencyDetails/latencyIndicator2")
      ).resolveOptionally(body.latencyDetails.map(_.latencyIndicator2))
    )

  private def validateQuarterlyTypeChoice(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    combine(
      resolveEnum(
        QuarterlyPeriodType.parser,
        QuarterlyPeriodTypeFormatError.withPath("/quarterlyTypeChoice/quarterlyPeriodType")
      ).resolveOptionally(body.quarterlyTypeChoice.map(_.quarterlyPeriodType)),
      ResolveTaxYear
        .resolverWithCustomErrors(
          TaxYearFormatError.withPath("/quarterlyTypeChoice/taxYearOfChoice"),
          RuleTaxYearRangeInvalidError.withPath("/quarterlyTypeChoice/taxYearOfChoice")
        )
        .resolveOptionally(body.quarterlyTypeChoice.map(_.taxYearOfChoice))
    )

  private def validateAccountingType(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    resolveEnum(AccountingType.parser, AccountingTypeFormatError.withPath("/accountingType")).resolveOptionally(body.accountingType).toUnit

  private def validateCommencementDate(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    ResolveIsoDate(body.commencementDate, DateFormatError.withPath("/commencementDate")).andThen { commencementDate =>
      validateCommencementDateRule(commencementDate)
    }

  private def validateCommencementDateRule(commencementDate: Option[LocalDate]): Validated[Seq[MtdError], Unit] =
    Validated.cond(
      commencementDate.forall(_.isBefore(LocalDate.now(ZoneOffset.UTC))),
      (),
      List(RuleCommencementDateNotSupportedError)
    )

  private def validateCessationDate(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    ResolveIsoDate(body.cessationDate, DateFormatError.withPath("/cessationDate")).toUnit

  private def validateBusinessAddress(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    combine(
      ResolveStringPattern(
        body.businessAddressLineOne,
        commonStringRegex,
        StringFormatError.withPath("/businessAddressLineOne")
      ),
      ResolveStringPattern(
        body.businessAddressLineTwo,
        commonStringRegex,
        StringFormatError.withPath("/businessAddressLineTwo")
      ),
      ResolveStringPattern(
        body.businessAddressLineThree,
        commonStringRegex,
        StringFormatError.withPath("/businessAddressLineThree")
      ),
      ResolveStringPattern(
        body.businessAddressLineFour,
        commonStringRegex,
        StringFormatError.withPath("/businessAddressLineFour")
      ),
      ResolveParsedAlpha2CountryCode(
        body.businessAddressCountryCode,
        "/businessAddressCountryCode"
      ),
      ResolveStringPattern(
        body.businessAddressPostcode,
        postcodeRegex,
        PostcodeFormatError.withPath("/businessAddressPostcode")
      ),
      validateBusinessAddressRule(body),
      validateMissingPostcode(body)
    )

  private def validateBusinessAddressRule(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] = {
    val haveSufficientBusinessAddress: Boolean = body.businessAddressLineOne.nonEmpty && body.businessAddressCountryCode.nonEmpty

    if (isProperty(body) && body.hasAnyBusinessAddressDetails) {
      Invalid(List(RuleUnexpectedBusinessAddressError))
    } else if (isSelfEmployment(body) && !haveSufficientBusinessAddress) {
      Invalid(List(RuleMissingBusinessAddressError))
    } else {
      valid
    }
  }

  private def validateMissingPostcode(body: CreateTestBusinessRequestBody): Validated[Seq[MtdError], Unit] =
    if (requiresBusinessPostcode(body) && body.businessAddressPostcode.isEmpty) {
      Invalid(List(MissingPostcodeError))
    } else {
      valid
    }

  private def requiresBusinessPostcode(body: CreateTestBusinessRequestBody): Boolean =
    isSelfEmployment(body) && body.businessAddressCountryCode.contains("GB")

  private def parsedTypeOfBusiness(body: CreateTestBusinessRequestBody): Option[TypeOfBusiness] =
    TypeOfBusiness.parser.lift(body.typeOfBusiness)

  private def isSelfEmployment(body: CreateTestBusinessRequestBody): Boolean = parsedTypeOfBusiness(body).exists(_.isSelfEmployment)

  private def isProperty(body: CreateTestBusinessRequestBody): Boolean = parsedTypeOfBusiness(body).exists(_.isProperty)
}

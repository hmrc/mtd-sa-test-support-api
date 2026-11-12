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

package api.models.errors

import play.api.http.Status.*

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)

object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)

object BusinessIdFormatError extends MtdError("FORMAT_BUSINESS_ID", "The provided Business ID is invalid", BAD_REQUEST)

object CheckpointIdFormatError extends MtdError("FORMAT_CHECKPOINT_ID", "The provided checkpoint ID is invalid", BAD_REQUEST)

object StringFormatError extends MtdError("FORMAT_STRING", "The supplied string format is not valid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object DateFormatError extends MtdError("FORMAT_DATE", "The supplied date format is not valid", BAD_REQUEST)

object StartDateFormatError extends MtdError("FORMAT_START_DATE", "The provided start date is invalid", BAD_REQUEST)

object EndDateFormatError extends MtdError("FORMAT_END_DATE", "The provided end date is invalid", BAD_REQUEST)

object TypeOfBusinessFormatError extends MtdError("FORMAT_TYPE_OF_BUSINESS", "The provided typeOfBusiness field is invalid", BAD_REQUEST)

object AccountingTypeFormatError extends MtdError("FORMAT_ACCOUNTING_TYPE", "The provided accountingType field is invalid", BAD_REQUEST)

object PostcodeFormatError extends MtdError("FORMAT_POSTCODE", "The provided businessAddressPostcode is invalid", BAD_REQUEST)

object LatencyIndicatorFormatError extends MtdError("FORMAT_LATENCY_INDICATOR", "The format of a latency indicator field is incorrect", BAD_REQUEST)

object CountryCodeFormatError extends MtdError("FORMAT_COUNTRY_CODE", "The provided country code is invalid", BAD_REQUEST) {
  def forRuleCountryCode: MtdError = copy(message = "The country code is not a valid ISO 3166-1 alpha-2 country code")
}

object StatusFormatError extends MtdError("FORMAT_STATUS", "The provided status is invalid", BAD_REQUEST)

object StatusReasonFormatError extends MtdError("FORMAT_STATUS_REASON", "The provided status reason is invalid", BAD_REQUEST)

object BusinessIncome2YearsPriorFormatError
    extends MtdError("FORMAT_BUSINESS_INCOME_2_YEARS_PRIOR", "The provided business Income 2 Years Prior is invalid", BAD_REQUEST)

object SubmittedOnFormatError extends MtdError("FORMAT_SUBMITTED_ON", "The provided submitted on date is invalid", BAD_REQUEST)

object QuarterlyPeriodTypeFormatError extends MtdError("FORMAT_QUARTERLY_PERIOD_TYPE", "The quarterly period type format is not valid", BAD_REQUEST)

// Rule Errors
object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleEndBeforeStartDateError extends MtdError("RULE_END_DATE_BEFORE_START_DATE", "The end date is before the start date", BAD_REQUEST)

object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "The tax year range is invalid", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RulePropertyBusinessAddedError extends MtdError("RULE_PROPERTY_BUSINESS_ADDED", "The provided property business is already added", BAD_REQUEST)

object RuleCommencementDateNotSupportedError
    extends MtdError("RULE_COMMENCEMENT_DATE_NOT_SUPPORTED", "The specified commencementDate must be in the past", BAD_REQUEST)

object RuleFirstAccountingDateRangeInvalidError
    extends MtdError("RULE_FIRST_ACCOUNTING_DATE_RANGE_INVALID", "The first accounting period is not a single complete tax year", BAD_REQUEST)

object RuleUnexpectedBusinessAddressError
    extends MtdError("RULE_UNEXPECTED_BUSINESS_ADDRESS", "Unexpected businessAddress provided for property business", BAD_REQUEST)

object RuleMissingBusinessAddressError
    extends MtdError("RULE_MISSING_BUSINESS_ADDRESS", "businessAddress not provided for the self-employment business", BAD_REQUEST)

object RuleUnexpectedTradingTypeError
    extends MtdError("RULE_UNEXPECTED_TRADING_TYPE", "Unexpected tradingType provided for property business", BAD_REQUEST)

object RuleMissingTradingTypeError
    extends MtdError("RULE_MISSING_TRADING_TYPE", "tradingType not provided for the self-employment business", BAD_REQUEST)

object RuleUnexpectedTradingNameError
    extends MtdError("RULE_UNEXPECTED_TRADING_NAME", "Unexpected tradingName provided for property business", BAD_REQUEST)

object RuleMissingTradingNameError
    extends MtdError("RULE_MISSING_TRADING_NAME", "tradingName not provided for the self-employment business", BAD_REQUEST)

object DuplicateSubmittedOnError extends MtdError("DUPLICATE_SUBMITTED_ON", "The provided submittedOn values are not unique", BAD_REQUEST)

object MissingPostcodeError extends MtdError("MISSING_POSTCODE", "Missing postcode", BAD_REQUEST)

object MissingFirstAccountingPeriodStartDateError
    extends MtdError("MISSING_FIRST_ACCOUNTING_PERIOD_START_DATE", "Missing firstAccountingPeriodStartDate value", BAD_REQUEST)

object MissingFirstAccountingPeriodEndDateError
    extends MtdError("MISSING_FIRST_ACCOUNTING_PERIOD_END_DATE", "Missing firstAccountingPeriodEndDate value", BAD_REQUEST)

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)

//Authorisation Errors
object ClientNotAuthenticatedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

object ClientNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)

//Stub errors
object RuleIncorrectGovTestScenarioError
    extends MtdError(code = "RULE_INCORRECT_GOV_TEST_SCENARIO", message = "The Gov-Test-Scenario was not found", BAD_REQUEST)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

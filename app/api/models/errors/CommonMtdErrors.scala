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

package api.models.errors

import play.api.http.Status._

// Format Errors
object NinoFormatError         extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object BusinessIdFormatError   extends MtdError("FORMAT_BUSINESS_ID", "The provided Business ID is invalid", BAD_REQUEST)
object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided Submission ID is invalid", BAD_REQUEST)
object FromDateFormatError     extends MtdError("FORMAT_FROM_DATE", "The provided From date is invalid", BAD_REQUEST)
object ToDateFormatError       extends MtdError("FORMAT_TO_DATE", "The provided To date is invalid", BAD_REQUEST)
object DateFormatError         extends MtdError("FORMAT_DATE", "The supplied date format is not valid", BAD_REQUEST)
object StringFormatError       extends MtdError("FORMAT_STRING", "The supplied string format is not valid", BAD_REQUEST)
object CountryCodeFormatError  extends MtdError("FORMAT_COUNTRY_CODE", "The provided Country code is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object TaxYearFormatError  extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object PeriodIdFormatError extends MtdError("FORMAT_PERIOD_ID", "The provided period id is invalid", BAD_REQUEST)

// Rule Errors
object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleBuildingNameNumberError
    extends MtdError("RULE_BUILDING_NAME_NUMBER", "Postcode must be supplied along with at least one of name or number", BAD_REQUEST)

object RuleToDateBeforeFromDateError
    extends MtdError("RULE_TO_DATE_BEFORE_FROM_DATE", "The To date cannot be earlier than the From date", BAD_REQUEST)

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code", BAD_REQUEST)

object RuleOverlappingPeriodError
    extends MtdError("RULE_OVERLAPPING_PERIOD", "Period summary overlaps with any of the existing period summaries", BAD_REQUEST)

object RuleMisalignedPeriodError    extends MtdError("RULE_MISALIGNED_PERIOD", "Period summary is not within the accounting period", BAD_REQUEST)
object RuleNotContiguousPeriodError extends MtdError("RULE_NOT_CONTIGUOUS_PERIOD", "Period summaries are not contiguous", BAD_REQUEST)

object RuleDuplicateSubmissionError
    extends MtdError("RULE_DUPLICATE_SUBMISSION", "A summary has already been submitted for the period specified", BAD_REQUEST)

object RuleTypeOfBusinessIncorrectError
    extends MtdError("RULE_TYPE_OF_BUSINESS_INCORRECT", "The businessId is for a different type of business", BAD_REQUEST)

object RuleDuplicateCountryCodeError
    extends MtdError("RULE_DUPLICATE_COUNTRY_CODE", "You cannot supply the same country code for multiple properties", BAD_REQUEST) {

  def forDuplicatedCodesAndPaths(code: String, paths: Seq[String]): MtdError =
    RuleDuplicateCountryCodeError.copy(message = s"The country code '$code' is duplicated for multiple properties", paths = Some(paths))

}

object RuleBothExpensesSuppliedError
    extends MtdError("RULE_BOTH_EXPENSES_SUPPLIED", "Both Expenses and Consolidated Expenses must not be present at the same time", BAD_REQUEST)

object RuleBothAllowancesSuppliedError
    extends MtdError("RULE_BOTH_ALLOWANCES_SUPPLIED", "Both allowances and property allowances must not be present at the same time", BAD_REQUEST)

object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleHistoricTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleTaxYearRangeInvalidError
    extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "Tax year range invalid. A tax year range of one year is required", BAD_REQUEST)

object RulePropertyIncomeAllowanceError
    extends MtdError(
      "RULE_PROPERTY_INCOME_ALLOWANCE",
      "The propertyIncomeAllowance cannot be submitted if privateUseAdjustment is supplied",
      BAD_REQUEST)

object RuleInvalidSubmissionPeriodError
    extends MtdError(
      "RULE_INVALID_SUBMISSION_PERIOD",
      "Property income and expenses submissions cannot be more than 10 days before the end of the Period",
      BAD_REQUEST)

object RuleInvalidSubmissionEndDateError
    extends MtdError("RULE_INVALID_SUBMISSION_END_DATE", "The submitted end date must be the end of the quarter", BAD_REQUEST)

// Missing Date Errors
object MissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing", BAD_REQUEST)
object MissingToDateError   extends MtdError("MISSING_TO_DATE", "The To date parameter is missing", BAD_REQUEST)

//Standard Errors
object NotFoundError             extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)
object SubmissionIdNotFoundError extends MtdError("SUBMISSION_ID_NOT_FOUND", "Submission ID not found", NOT_FOUND)

object InternalError           extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)
object BadRequestError         extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)
object BVRError                extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error", INTERNAL_SERVER_ERROR)

//Authorisation Errors
object ClientNotAuthenticatedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)
object InvalidBearerTokenError     extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)
object ClientNotAuthorisedError    extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)

//Stub errors
object RuleIncorrectGovTestScenarioError
    extends MtdError(code = "RULE_INCORRECT_GOV_TEST_SCENARIO", message = "The Gov-Test-Scenario was not found", BAD_REQUEST)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)
object UnsupportedVersionError  extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)
object InvalidBodyTypeError     extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

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

import play.api.http.Status.BAD_REQUEST

object CheckpointIdFormatError        extends MtdError("FORMAT_CHECKPOINT_ID", "The provided checkpoint ID is invalid", BAD_REQUEST)
object RulePropertyBusinessAddedError extends MtdError("RULE_PROPERTY_BUSINESS_ADDED", "The provided property business is already added", BAD_REQUEST)
object TypeOfBusinessFormatError      extends MtdError("FORMAT_TYPE_OF_BUSINESS", "The provided typeOfBusiness field is invalid", BAD_REQUEST)
object AccountingTypeFormatError      extends MtdError("FORMAT_ACCOUNTING_TYPE", "The provided accountingType field is invalid", BAD_REQUEST)

object RuleCommencementDateNotSupported
    extends MtdError("RULE_COMMENCEMENT_DATE_NOT_SUPPORTED", "The specified commencementDate must be in the past", BAD_REQUEST)

object PostcodeFormatError         extends MtdError("FORMAT_POSTCODE", "The provided businessAddressPostcode is invalid", BAD_REQUEST)
object LatencyIndicatorFormatError extends MtdError("FORMAT_LATENCY_INDICATOR", "The format of a latency indicator field is incorrect", BAD_REQUEST)
object MissingPostcodeError        extends MtdError("MISSING_POSTCODE", "Missing postcode", BAD_REQUEST)
object CountryCodeFormatError      extends MtdError("FORMAT_COUNTRY_CODE", "The provided country code is invalid", BAD_REQUEST)
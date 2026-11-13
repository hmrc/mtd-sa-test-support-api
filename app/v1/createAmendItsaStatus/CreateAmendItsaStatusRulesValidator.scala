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

package v1.createAmendItsaStatus

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.*
import api.models.errors.*
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toFoldableOps
import v1.createAmendItsaStatus.model.request.*

object CreateAmendItsaStatusRulesValidator extends RulesValidator[CreateAmendItsaStatusRequestData] {

  override def validateBusinessRules(parsed: CreateAmendItsaStatusRequestData): Validated[Seq[MtdError], CreateAmendItsaStatusRequestData] =
    combine(
      validateItsaStatusDetails(parsed.body),
      validateSubmittedOnUniqueness(parsed.body)
    ).onSuccess(parsed)

  private def validateItsaStatusDetails(body: CreateAmendItsaStatusRequestBody): Validated[Seq[MtdError], Unit] =
    body.itsaStatusDetails.zipWithIndex.traverse_ { (detail, index) =>
      combine(
        validateSubmittedOn(detail.submittedOn, index),
        validateStatus(detail.status, index),
        validateStatusReason(detail.statusReason, index),
        validateBusinessIncome2YearsPrior(detail.businessIncome2YearsPrior, index)
      )
    }

  private def validateSubmittedOn(submittedOn: String, index: Int): Validated[Seq[MtdError], Unit] =
    ResolveInstant(
      submittedOn,
      SubmittedOnFormatError.withPath(s"/itsaStatusDetails/$index/submittedOn")
    ).toUnit

  private def validateStatus(status: String, index: Int): Validated[Seq[MtdError], Unit] =
    resolveEnum(
      Status.parser,
      StatusFormatError.withPath(s"/itsaStatusDetails/$index/status")
    )(status).toUnit

  private def validateStatusReason(statusReason: String, index: Int): Validated[Seq[MtdError], Unit] =
    resolveEnum(
      StatusReason.parser,
      StatusReasonFormatError.withPath(s"/itsaStatusDetails/$index/statusReason")
    )(statusReason).toUnit

  private def validateBusinessIncome2YearsPrior(businessIncome2YearsPrior: Option[BigDecimal], index: Int): Validated[Seq[MtdError], Unit] =
    ResolveParsedNumber()(
      businessIncome2YearsPrior,
      BusinessIncome2YearsPriorFormatError.withPath(s"/itsaStatusDetails/$index/businessIncome2YearsPrior")
    ).toUnit

  private def validateSubmittedOnUniqueness(body: CreateAmendItsaStatusRequestBody): Validated[Seq[MtdError], Unit] = {
    val submittedOnTimestamps: Seq[String] = body.itsaStatusDetails.map(_.submittedOn)

    if (submittedOnTimestamps.size == submittedOnTimestamps.distinct.size) valid else Invalid(List(DuplicateSubmittedOnError))
  }

}

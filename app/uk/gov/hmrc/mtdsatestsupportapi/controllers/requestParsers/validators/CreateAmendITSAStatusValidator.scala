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
import api.controllers.requestParsers.validators.validations.{DateValidation, JsonFormatValidation, NinoValidation, TaxYearValidation}
import api.models.errors.{ MtdError, RuleIncorrectOrEmptyBodyError, DuplicateSubmittedErrorOn}
import play.api.libs.json.JsArray
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations.{
  BusinessIncome2YearsPriorValidation,
  StatusReasonValidation,
  StatusValidation
}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRequestBody.format
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{
  CreateAmendITSAStatusRawData,
  CreateAmendITSAStatusRequestBody,
  ITSAStatusDetail
}

class CreateAmendITSAStatusValidator extends Validator[CreateAmendITSAStatusRawData] {

  private val validationSet =
    List(parameterFormatValidation, enumFieldsValidation, bodyFormatValidation, bodyValidation, submissionDatesUniquenessValidation())

  override def validate(data: CreateAmendITSAStatusRawData): List[MtdError] = {

    val result = run(validationSet, data).distinct
    result
  }

  private def parameterFormatValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = (data: CreateAmendITSAStatusRawData) =>
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )

  private def enumFieldsValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = (data: CreateAmendITSAStatusRawData) => {
    (data.body \ "itsaStatusDetails").asOpt[JsArray] match {

      case Some(itsaStatusDetailsJson) =>
        if (itsaStatusDetailsJson.value.isEmpty) { List(List(RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails"))) }
        else {
          itsaStatusDetailsJson.value.zipWithIndex.map { case (itsaStatusDetailJson, index) =>
            val maybeStatusErrors = (itsaStatusDetailJson \ "status").asOpt[String] match {
              case Some(status) => StatusValidation.validate(status, path = Some(s"/itsaStatusDetails/$index/status"))
              case None         => List(RuleIncorrectOrEmptyBodyError.withExtraPath(s"/itsaStatusDetails/$index/status"))
            }

            val maybeStatusReasonErrors = (itsaStatusDetailJson \ "statusReason").asOpt[String] match {
              case Some(statusReason) => StatusReasonValidation.validate(statusReason, path = Some(s"/itsaStatusDetails/$index/statusReason"))
              case None               => List(RuleIncorrectOrEmptyBodyError.withExtraPath(s"/itsaStatusDetails/$index/statusReason"))
            }

            (maybeStatusErrors ++ maybeStatusReasonErrors).toList
          }
        }.toList
      case None => List(List(RuleIncorrectOrEmptyBodyError))
    }

  }

  private def submissionDatesUniquenessValidation(
      error: MtdError = DuplicateSubmittedErrorOn): CreateAmendITSAStatusRawData => List[List[MtdError]] =
    (data: CreateAmendITSAStatusRawData) => {

      (data.body \ "itsaStatusDetails").asOpt[JsArray] match {

        case Some(itsaStatusDetailsJson) =>
          val submissionDatesAreUnique = itsaStatusDetailsJson.value
            .map(details => (details \ "submittedOn").asOpt[String])
            .collect { case Some(timestamp) => timestamp }
            .toSet
            .size == itsaStatusDetailsJson.value.size
          if (submissionDatesAreUnique) Nil else List(List(error))
        case None => Nil
      }
    }

  private def bodyFormatValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = { data =>
    JsonFormatValidation.validate[CreateAmendITSAStatusRequestBody](data.body) match {
      case Nil          => Nil
      case schemaErrors => List(schemaErrors)
    }
  }

  private def bodyValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = (data: CreateAmendITSAStatusRawData) => {
    val result = data.body
      .as[CreateAmendITSAStatusRequestBody]
      .itsaStatusDetails
      .zipWithIndex
      .map { case (entry, index) => validateItsaStatusDetails(entry, index) }
      .toList

    result
  }

  private def validateItsaStatusDetails(detail: ITSAStatusDetail, index: Int): List[MtdError] = {
    import detail._

    val submittedOnErrors = DateValidation.validateSubmittedOn(submittedOn).map(_.withExtraPath(s"/itsaStatusDetails/$index/submittedOn"))

    val businessIncomeErrors =
      BusinessIncome2YearsPriorValidation
        .validateOptional(businessIncome2YearsPrior, s"/itsaStatusDetails/$index/businessIncome2YearsPrior")

    (submittedOnErrors ++ businessIncomeErrors).toList
  }

}

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
import api.controllers.requestParsers.validators.validations.{DateValidation, NinoValidation, TaxYearValidation}
import api.models.errors.MtdError
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations.{BusinessIncome2YearsPriorValidation, StatusValidation}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{CreateAmendITSAStatusRawData, CreateAmendITSAStatusRequestBody}

import javax.inject.Inject

class CreateAmendITSAStatusValidator @Inject() extends Validator[CreateAmendITSAStatusRawData] {

  private val validationSet =
    List(parameterFormatValidation, bodyValidation)

  override def validate(data: CreateAmendITSAStatusRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = (data: CreateAmendITSAStatusRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyValidation: CreateAmendITSAStatusRawData => List[List[MtdError]] = (data: CreateAmendITSAStatusRawData) => {
    val body = data.body.as[CreateAmendITSAStatusRequestBody]

    List(bodyFieldValidation(body))
  }

  private def bodyFieldValidation(body: CreateAmendITSAStatusRequestBody): List[MtdError] = {

    val submittedOnError: Seq[MtdError] = body.itsaStatusDetails.map(_.submittedOn).flatMap(DateValidation.validateSubmittedOn(_))
    val statusError: Seq[MtdError]      = body.itsaStatusDetails.flatMap(status => StatusValidation.validate(status.status.toString))
    val statusReasonError: Seq[MtdError] =
      body.itsaStatusDetails.flatMap(statusReason => StatusValidation.validate(statusReason.statusReason.toString))
    val businessError: Seq[MtdError] =
      body.itsaStatusDetails.map(_.businessIncome2YearsPrior).flatMap(BusinessIncome2YearsPriorValidation.validateOptional(_, "/path"))

    (submittedOnError ++ statusError ++ statusReasonError ++ businessError).toList
  }

}

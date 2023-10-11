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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.EnumValidation
import api.models.errors.{MtdError, StatusReasonFormatError}
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReason

object StatusReasonValidation extends EnumValidation[StatusReason] {
  override protected val parser: PartialFunction[String, StatusReason] = StatusReason.parser

  def validate(value: String, error: => MtdError = StatusReasonFormatError, path: Option[String] = None): Seq[MtdError] =
    validateEnum(value, path.map(error.withExtraPath).getOrElse(error))

}

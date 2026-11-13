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

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v1.createAmendItsaStatus.CreateAmendItsaStatusRulesValidator.validateBusinessRules
import v1.createAmendItsaStatus.model.request.{CreateAmendItsaStatusRequestBody, CreateAmendItsaStatusRequestData}

import javax.inject.Inject

class CreateAmendItsaStatusValidator @Inject() (nino: String, taxYear: String, body: JsValue) extends Validator[CreateAmendItsaStatusRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAmendItsaStatusRequestBody]()

  def validate: Validated[Seq[MtdError], CreateAmendItsaStatusRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateAmendItsaStatusRequestData.apply) andThen validateBusinessRules

}

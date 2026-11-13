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

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import play.api.libs.json.JsValue
import v1.createTestBusiness.CreateTestBusinessRulesValidator.validateBusinessRules
import v1.createTestBusiness.model.request.{CreateTestBusinessRequestBody, CreateTestBusinessRequestData}

import javax.inject.Inject

class CreateTestBusinessValidator @Inject() (nino: String, body: JsValue) extends Validator[CreateTestBusinessRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[CreateTestBusinessRequestBody]()

  def validate: Validated[Seq[MtdError], CreateTestBusinessRequestData] =
    (
      ResolveNino(nino),
      resolveJson(body)
    ).mapN(CreateTestBusinessRequestData.apply) andThen validateBusinessRules

}

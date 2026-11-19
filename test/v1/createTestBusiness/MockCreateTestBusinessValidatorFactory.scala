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

import api.controllers.validators.{MockValidatorFactory, Validator}
import org.scalamock.handlers.CallHandler
import play.api.libs.json.JsValue
import v1.createTestBusiness.model.request.CreateTestBusinessRequestData

trait MockCreateTestBusinessValidatorFactory extends MockValidatorFactory[CreateTestBusinessRequestData] {

  val mockCreateTestBusinessValidatorFactory: CreateTestBusinessValidatorFactory = mock[CreateTestBusinessValidatorFactory]

  def validator(): CallHandler[Validator[CreateTestBusinessRequestData]] =
    (mockCreateTestBusinessValidatorFactory.validator(_: String, _: JsValue)).expects(*, *)

}

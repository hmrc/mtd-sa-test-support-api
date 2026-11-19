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
import api.utils.UnitSpec
import play.api.libs.json.JsObject
import v1.createAmendItsaStatus.model.request.CreateAmendItsaStatusRequestData

class CreateAmendItsaStatusValidatorFactorySpec extends UnitSpec {

  private val validatorFactory: CreateAmendItsaStatusValidatorFactory = new CreateAmendItsaStatusValidatorFactory

  "CreateAmendItsaStatusValidatorFactory" should {
    "return CreateAmendItsaStatusValidator" in {
      val result: Validator[CreateAmendItsaStatusRequestData] = validatorFactory.validator(
        nino = "AA123456A",
        taxYear = "2023-24",
        body = JsObject.empty
      )

      result shouldBe a[CreateAmendItsaStatusValidator]
    }
  }

}

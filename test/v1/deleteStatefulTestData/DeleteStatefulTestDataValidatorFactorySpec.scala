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

package v1.deleteStatefulTestData

import api.controllers.validators.Validator
import api.utils.UnitSpec
import v1.deleteStatefulTestData.model.request.DeleteStatefulTestDataRequestData

class DeleteStatefulTestDataValidatorFactorySpec extends UnitSpec {

  private val validatorFactory: DeleteStatefulTestDataValidatorFactory = new DeleteStatefulTestDataValidatorFactory

  "DeleteStatefulTestDataValidatorFactory" should {
    "return DeleteStatefulTestDataValidator" in {
      val result: Validator[DeleteStatefulTestDataRequestData] = validatorFactory.validator(
        vendorClientId = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7",
        nino = Some("AA123456A")
      )

      result shouldBe a[DeleteStatefulTestDataValidator]
    }
  }

}

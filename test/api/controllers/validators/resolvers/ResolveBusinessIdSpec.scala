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

package api.controllers.validators.resolvers

import api.models.domain.BusinessId
import api.models.errors.{BusinessIdFormatError, MtdError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

class ResolveBusinessIdSpec extends UnitSpec {

  "ResolveBusinessId" should {
    "return no errors" when {
      "given a valid business ID" in {
        val validBusinessId: String                      = "XAIS12345678901"
        val result: Validated[Seq[MtdError], BusinessId] = ResolveBusinessId(validBusinessId)

        result shouldBe Valid(BusinessId(validBusinessId))
      }
    }

    "return an error" when {
      "given an invalid business ID" in {
        val invalidBusinessId: String                    = "XAXAIS65271982AD"
        val result: Validated[Seq[MtdError], BusinessId] = ResolveBusinessId(invalidBusinessId)

        result shouldBe Invalid(List(BusinessIdFormatError))
      }
    }
  }

}

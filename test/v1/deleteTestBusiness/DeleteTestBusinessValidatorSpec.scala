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

package v1.deleteTestBusiness

import api.models.domain.{BusinessId, Nino}
import api.models.errors.*
import api.utils.UnitSpec
import v1.deleteTestBusiness.model.request.DeleteTestBusinessRequestData

class DeleteTestBusinessValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val nino: String       = "AA123456A"
  private val businessId: String = "X0IS12345678901"

  private def validate(nino: String = nino, businessId: String = businessId) =
    new DeleteTestBusinessValidator(nino, businessId).validateAndWrapResult()

  "DeleteTestBusinessValidator" should {
    "return the parsed object" when {
      "a valid request is supplied" in {
        validate() shouldBe Right(DeleteTestBusinessRequestData(Nino(nino), BusinessId(businessId)))
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validate("AA1234") shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return BusinessIdFormatError" when {
      "an invalid business ID is supplied" in {
        validate(businessId = "X0IS12") shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }
    }

    "return multiple errors" when {
      "both nino and business ID are invalid" in {
        validate("AA1234", "X0IS12") shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(Seq(BusinessIdFormatError, NinoFormatError))
          )
        )
      }
    }
  }

}

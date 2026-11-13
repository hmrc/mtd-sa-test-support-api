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

package api.controllers.resolvers

import api.models.domain.Nino
import api.models.errors.NinoFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveNinoSpec extends UnitSpec {

  "ResolveNino" should {
    "resolve a valid NINO" in {
      ResolveNino("AA123456A") shouldBe Valid(Nino("AA123456A"))
    }

    "reject a reserved NINO prefix" in {
      ResolveNino("BG123456A") shouldBe Invalid(Seq(NinoFormatError))
    }

    "reject malformed and null values" in {
      ResolveNino("aa123456a") shouldBe Invalid(Seq(NinoFormatError))
      ResolveNino(null) shouldBe Invalid(Seq(NinoFormatError))
    }
  }

}

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

import api.models.errors.{DateFormatError, MtdError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.LocalDate

class ResolveIsoDateSpec extends UnitSpec {

  "ResolveIsoDate" should {
    "return no errors" when {
      "given a valid date" in {
        val result: Validated[Seq[MtdError], LocalDate] = ResolveIsoDate("2026-04-06", DateFormatError)

        result shouldBe Valid(LocalDate.parse("2026-04-06"))
      }

      "given a valid date in an Option" in {
        val result: Validated[Seq[MtdError], Option[LocalDate]] = ResolveIsoDate(Some("2026-04-06"), DateFormatError)

        result shouldBe Valid(Some(LocalDate.parse("2026-04-06")))
      }

      "given an empty Option" in {
        val result: Validated[Seq[MtdError], Option[LocalDate]] = ResolveIsoDate(None, DateFormatError)

        result shouldBe Valid(None)
      }
    }

    "return the expected error" when {
      "given an invalid date" in {
        val result: Validated[Seq[MtdError], LocalDate] = ResolveIsoDate("2024", DateFormatError)

        result shouldBe Invalid(List(DateFormatError))
      }

      "given an invalid date in an Option" in {
        val result: Validated[Seq[MtdError], Option[LocalDate]] = ResolveIsoDate(Some("2024"), DateFormatError)

        result shouldBe Invalid(List(DateFormatError))
      }
    }

  }

}

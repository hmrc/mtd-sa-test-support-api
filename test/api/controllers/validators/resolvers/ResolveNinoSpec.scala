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

import api.models.domain.Nino
import api.models.errors.NinoFormatError
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}

class ResolveNinoSpec extends UnitSpec {

  "ResolveNino" should {
    "return the parsed Nino" when {
      "given a valid nino string" in
        expectSuccess("AA123456A")
    }

    "return an error" when {
      "given an invalid Nino" in
        expectError("AA123456ABCBBCBCBC")

      "given a valid number with spaces" in
        expectError("AB 12 34 56 C")

      "given a valid number with a leading space" in
        expectError(" AB123456C")

      "given a valid number with a trailing space" in
        expectError("AB123456C ")

      "given an empty string" in
        expectError("")

      "given only spaces" in
        expectError("    ")

      "given only digits" in
        expectError("123456")

      "given non-alphanum characters" in
        expectError("@£%!)(*&^")

      "given only one starting letter" in
        expectError("A123456C")

      "given only one starting letter and a slightly longer number" in
        expectError("A1234567C")

      "given three starting letter" in
        expectError("ABC12345C")

      "given three starting letter and a slightly longer number" in
        expectError("ABC123456C")

      "given lowercase letters" in
        expectError("ab123456c")

      "given less than 6 middle digits" in
        expectError("AB12345C")

      "given more than 6 middle digits" in
        expectError("AB1234567C")

      "given O as the second letter" in
        expectError("AO123456C")

      "given E as the suffix" in
        expectError("AB123456E")

      "given invalid prefixes" in {
        val invalidStartLetterCombinations = List('D', 'F', 'I', 'Q', 'U', 'V').combinations(2).map(_.mkString("")).toList
        val invalidPrefixes                = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")
        for (v <- invalidStartLetterCombinations ::: invalidPrefixes) {
          val invalidNino = v + "123456C"
          withClue(s"Invalid nino: $invalidNino") {
            expectError(invalidNino)
          }
        }
      }

    }

  }

  private def expectSuccess(validNino: String): Unit = {
    val result = ResolveNino(validNino)
    result shouldBe Valid(Nino(validNino))
  }

  private def expectError(invalidNino: String): Unit = {
    val result = ResolveNino(invalidNino)
    result shouldBe Invalid(List(NinoFormatError))
  }

}

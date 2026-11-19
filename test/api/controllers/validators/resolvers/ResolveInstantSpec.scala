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

import api.models.errors.{MtdError, SubmittedOnFormatError}
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.Instant
import java.time.format.DateTimeFormatter

class ResolveInstantSpec extends UnitSpec {

  private val defaultTimestamp: String        = "2026-10-18T12:30:45.123Z"
  private val expectedDefaultInstant: Instant = Instant.parse(defaultTimestamp)

  private val customFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

  private val customTimestamp: String        = "2026-10-18T12:30:45Z"
  private val expectedCustomInstant: Instant = Instant.parse(customTimestamp)

  "ResolveInstant" should {
    "return no errors" when {
      "given a timestamp matching the default format" in {
        val result: Validated[Seq[MtdError], Instant] = ResolveInstant(defaultTimestamp, SubmittedOnFormatError)

        result shouldBe Valid(expectedDefaultInstant)
      }

      "given a matching timestamp in an Option matching the default format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(Some(defaultTimestamp), SubmittedOnFormatError)

        result shouldBe Valid(Some(expectedDefaultInstant))
      }

      "given an empty Option and the default format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(None, SubmittedOnFormatError)

        result shouldBe Valid(None)
      }

      "given a timestamp matching a custom format" in {
        val result: Validated[Seq[MtdError], Instant] = ResolveInstant(customTimestamp, SubmittedOnFormatError, customFormatter)

        result shouldBe Valid(expectedCustomInstant)
      }

      "given a timestamp in an Option matching a custom format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(Some(customTimestamp), SubmittedOnFormatError, customFormatter)

        result shouldBe Valid(Some(expectedCustomInstant))
      }

      "given an empty Option and a custom format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(None, SubmittedOnFormatError, customFormatter)

        result shouldBe Valid(None)
      }
    }

    "return the expected error" when {
      "given a timestamp that does not match the default format" in {
        val result: Validated[Seq[MtdError], Instant] = ResolveInstant(customTimestamp, SubmittedOnFormatError)

        result shouldBe Invalid(Seq(SubmittedOnFormatError))
      }

      "given a timestamp in an Option that does not match the default format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(Some(customTimestamp), SubmittedOnFormatError)

        result shouldBe Invalid(Seq(SubmittedOnFormatError))
      }

      "given a timestamp that does not match a custom format" in {
        val result: Validated[Seq[MtdError], Instant] = ResolveInstant(defaultTimestamp, SubmittedOnFormatError, customFormatter)

        result shouldBe Invalid(Seq(SubmittedOnFormatError))
      }

      "given a timestamp in an Option that does not match a custom format" in {
        val result: Validated[Seq[MtdError], Option[Instant]] = ResolveInstant(Some(defaultTimestamp), SubmittedOnFormatError, customFormatter)

        result shouldBe Invalid(Seq(SubmittedOnFormatError))
      }
    }
  }

}

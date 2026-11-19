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

import api.models.domain.DateRange
import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.LocalDate

class ResolveDateRangeSpec extends UnitSpec {

  private val validStart: String = "2023-06-21"
  private val validEnd: String   = "2024-06-21"

  private val sameValidStart: String = "2023-06-21"
  private val sameValidEnd: String   = "2023-06-21"

  private val startDateFormatError: MtdError    = StartDateFormatError.withPath("pathToStartDate")
  private val endDateFormatError: MtdError      = EndDateFormatError.withPath("pathToEndDate")
  private val endBeforeStartDateError: MtdError = RuleEndBeforeStartDateError.withPath("somePath")

  private val resolveDateRange: ResolveDateRange = ResolveDateRange(
    startDateFormatError = startDateFormatError,
    endDateFormatError = endDateFormatError,
    endBeforeStartDateError = endBeforeStartDateError
  )

  private val defaultResolveDateRange: ResolveDateRange = ResolveDateRange()

  "ResolveDateRange" should {
    "return no errors" when {
      "passed a valid start and end date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validStart -> validEnd)

        result shouldBe Valid(DateRange(LocalDate.parse(validStart), LocalDate.parse(validEnd)))
      }

      "passed an end date equal to start date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(sameValidStart -> sameValidEnd)

        result shouldBe Valid(DateRange(LocalDate.parse(sameValidStart), LocalDate.parse(sameValidEnd)))
      }

      "passed valid optional start and end dates" in {
        val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
          resolveDateRange(Some(validStart), Some(validEnd))

        result shouldBe Valid((Some(LocalDate.parse(validStart)), Some(LocalDate.parse(validEnd))))
      }

      "passed valid optional start and end dates that are equal" in {
        val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
          resolveDateRange(Some(sameValidStart), Some(sameValidEnd))

        result shouldBe Valid((Some(LocalDate.parse(sameValidStart)), Some(LocalDate.parse(sameValidEnd))))
      }

      "passed valid optional start date and no end date" in {
        val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
          resolveDateRange(Some(validStart), None)

        result shouldBe Valid((Some(LocalDate.parse(validStart)), None))
      }

      "passed no start date and valid optional end date" in {
        val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
          resolveDateRange(None, Some(validEnd))

        result shouldBe Valid((None, Some(LocalDate.parse(validEnd))))
      }

      "passed no start date and no end date" in {
        val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] = resolveDateRange(None, None)

        result shouldBe Valid((None, None))
      }
    }

    "return an error" when {
      "using default errors" when {
        "passed an invalid start date" in {
          val result: Validated[Seq[MtdError], DateRange] = defaultResolveDateRange("not-a-date" -> validEnd)

          result shouldBe Invalid(List(StartDateFormatError))
        }

        "passed an invalid end date" in {
          val result: Validated[Seq[MtdError], DateRange] = defaultResolveDateRange(validStart -> "not-a-date")

          result shouldBe Invalid(List(EndDateFormatError))
        }

        "passed an end date before start date" in {
          val result: Validated[Seq[MtdError], DateRange] = defaultResolveDateRange(validEnd -> validStart)

          result shouldBe Invalid(List(RuleEndBeforeStartDateError))
        }

        "passed an invalid optional start date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            defaultResolveDateRange(Some("not-a-date"), Some(validEnd))

          result shouldBe Invalid(List(StartDateFormatError))
        }

        "passed an invalid optional end date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            defaultResolveDateRange(Some(validStart), Some("not-a-date"))

          result shouldBe Invalid(List(EndDateFormatError))
        }

        "passed an optional end date before the optional start date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            defaultResolveDateRange(Some(validEnd), Some(validStart))

          result shouldBe Invalid(List(RuleEndBeforeStartDateError))
        }
      }

      "using custom errors" when {
        "passed an invalid start date" in {
          val result: Validated[Seq[MtdError], DateRange] = resolveDateRange("not-a-date" -> validEnd)

          result shouldBe Invalid(List(startDateFormatError))
        }

        "passed an invalid end date" in {
          val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validStart -> "not-a-date")

          result shouldBe Invalid(List(endDateFormatError))
        }

        "passed an end date before start date" in {
          val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validEnd -> validStart)

          result shouldBe Invalid(List(endBeforeStartDateError))
        }

        "passed an invalid optional start date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            resolveDateRange(Some("not-a-date"), Some(validEnd))

          result shouldBe Invalid(List(startDateFormatError))
        }

        "passed an invalid optional end date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            resolveDateRange(Some(validStart), Some("not-a-date"))

          result shouldBe Invalid(List(endDateFormatError))
        }

        "passed an optional end date before the optional start date" in {
          val result: Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
            resolveDateRange(Some(validEnd), Some(validStart))

          result shouldBe Invalid(List(endBeforeStartDateError))
        }
      }
    }
  }

}

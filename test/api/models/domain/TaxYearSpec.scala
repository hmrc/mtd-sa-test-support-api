/*
 * Copyright 2023 HM Revenue & Customs
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

package api.models.domain

import org.scalatest.prop.TableDrivenPropertyChecks
import support.UnitSpec

import java.time.LocalDate

class TaxYearSpec extends UnitSpec with TableDrivenPropertyChecks {

  "a TaxYear" when {
    "constructed from a date" must {
      "be the expected year, taking into account the UK tax year start date" in {
        forAll(
          Table(
            ("date", "end year"),
            ("2025-01-01", 2025),
            ("2025-04-01", 2025),
            ("2025-04-06", 2026),
            ("2023-06-01", 2024),
            ("2026-01-01", 2026),
            ("2021-12-31", 2022))) { case (date, expectedYear) =>
          withClue(s"Given $date:") {
            val result = TaxYear.containing(LocalDate.parse(date))
            result.endYear shouldBe expectedYear
          }
        }
      }
    }

    "constructed from TYS format" must {
      "work" in {
        TaxYear.fromTys("23-24") shouldBe TaxYear.ending(2024)
      }
    }

    "constructed from YYYY format" must {
      "work" in {
        TaxYear.fromDownstream("2024") shouldBe TaxYear.ending(2024)
      }
    }

    "constructed from MTD format" must {
      "work" in {
        TaxYear.fromMtd("2023-24") shouldBe TaxYear.ending(2024)
      }
    }

    "getting in TYS format" must {
      "work" in {
        TaxYear.ending(2024).asTys shouldBe "23-24"
      }
    }

    "getting in YYYY format" must {
      "work" in {
        TaxYear.ending(2024).asDownstream shouldBe "2024"
      }
    }

    "getting in MTD format" must {
      "work" in {
        TaxYear.ending(2024).asMtd shouldBe "2023-24"
      }
    }

    "getting the start and end" must {
      "get April 5th and April 6th when ending on non-leap years" in {
        val taxYear = TaxYear.ending(2023)

        taxYear.startDate shouldBe LocalDate.parse("2022-04-06")
        taxYear.endDate shouldBe LocalDate.parse("2023-04-05")
      }

      "get April 5th and April 6th when ending on leap years" in {
        val taxYear = TaxYear.ending(2020)

        taxYear.startDate shouldBe LocalDate.parse("2019-04-06")
        taxYear.endDate shouldBe LocalDate.parse("2020-04-05")
      }
    }
  }

}

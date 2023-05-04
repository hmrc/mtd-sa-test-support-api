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

package api.support

import java.time.LocalDate

import support.UnitSpec

class DateUtilsSpec extends UnitSpec {

  class TestDateUtilsAprilFourth extends DateUtils {
    override lazy val currentDate: LocalDate = LocalDate.parse("2020-04-04")
  }

  class TestDateUtilsAprilFifth extends DateUtils {
    override lazy val currentDate: LocalDate = LocalDate.parse("2020-04-05")
  }

  class TestDateUtilsAprilSixth extends DateUtils {
    override lazy val currentDate: LocalDate = LocalDate.parse("2020-04-06")
  }

  class TestDateUtilsAprilSeventh extends DateUtils {
    override lazy val currentDate: LocalDate = LocalDate.parse("2020-04-07")
  }

  "currentTaxYearStart" should {
    "return 2020-04-05" when {
      "currentDate is April 4th 2020" in {
        val dateUtils = new TestDateUtilsAprilFourth()
        dateUtils.currentTaxYearStart shouldBe "2019-04-06"
      }
      "currentDate is April 5th 2020" in {
        val dateUtils = new TestDateUtilsAprilFifth()
        dateUtils.currentTaxYearStart shouldBe "2019-04-06"
      }
    }
    "return 2021-04-05" when {
      "currentDate is April 6th 2020" in {
        val dateUtils = new TestDateUtilsAprilSixth()
        dateUtils.currentTaxYearStart shouldBe "2020-04-06"
      }
      "currentDate is April 7th 2020" in {
        val dateUtils = new TestDateUtilsAprilSeventh()
        dateUtils.currentTaxYearStart shouldBe "2020-04-06"
      }
    }
  }

  "currentTaxYearEnd" should {
    "return 2020-04-05" when {
      "currentDate is April 4th 2020" in {
        val dateUtils = new TestDateUtilsAprilFourth()
        dateUtils.currentTaxYearEnd shouldBe "2020-04-05"
      }
      "currentDate is April 5th 2020" in {
        val dateUtils = new TestDateUtilsAprilFifth()
        dateUtils.currentTaxYearEnd shouldBe "2020-04-05"
      }
    }
    "return 2021-04-05" when {
      "currentDate is April 6th 2020" in {
        val dateUtils = new TestDateUtilsAprilSixth()
        dateUtils.currentTaxYearEnd shouldBe "2021-04-05"
      }
      "currentDate is April 7th 2020" in {
        val dateUtils = new TestDateUtilsAprilSeventh()
        dateUtils.currentTaxYearEnd shouldBe "2021-04-05"
      }
    }
  }

  "currentDate" should {
    "return today" when {
      "called" in {
        val dateUtils = new DateUtils()
        dateUtils.currentDate.atStartOfDay() shouldBe LocalDate.now().atStartOfDay()
      }
    }
  }
}

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

package uk.gov.hmrc.internaltestsupport.utils

import java.time.Instant
import support.UnitSpec

class DateUtilsSpec extends UnitSpec {

  "DateUtils.nowAsUtc" should {
    "return a UTC timestamp in the expected ISO format" in {
      val timestamp = DateUtils.nowAsUtc

      timestamp should fullyMatch regex "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"
      noException should be thrownBy Instant.parse(timestamp)
    }
  }

}

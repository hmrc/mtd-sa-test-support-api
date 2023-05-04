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
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
class DateUtils {
  def currentDate: LocalDate   = LocalDate.now()
  private def limit: LocalDate = LocalDate.parse(s"${currentDate.getYear}-04-06", DateTimeFormatter.ISO_DATE)

  def currentTaxYearStart: String =
    if (currentDate.isBefore(limit)) limit.minusYears(1).toString else limit.toString

  def currentTaxYearEnd: String =
    if (currentDate.isBefore(limit)) limit.minusDays(1).toString else limit.plusYears(1).minusDays(1).toString

}

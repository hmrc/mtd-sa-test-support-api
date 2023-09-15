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

import utils.DateTimeSupport

import scala.math.Ordering.Implicits._
import java.time.LocalDate

/** Opaque wrapper around tax years, irrespective of their format/presentation. */
sealed abstract case class TaxYear(endYear: Int) {
  def startYear: Int = endYear - 1

  def startDate: LocalDate = TaxYear.startInYear(startYear)
  def endDate: LocalDate   = startDate.plusYears(1).minusDays(1)

  /** The tax year as a string in a YY-YY format e.g. "23-24".
    */
  def asTys: String = {
    val year2 = endYear - 2000
    val year1 = year2 - 1
    s"$year1-$year2"
  }

  /** The tax year as a string in a YYYY format e.g. "2024".
    */
  def asDownstream: String = endYear.toString

  /** The tax year as a string in a YYYY format e.g. "2023-24".
    */
  def asMtd: String = {
    val year2 = endYear - 2000
    s"$startYear-$year2"
  }

  override def toString: String = s"TaxYear($startYear - $endYear)"

}

object TaxYear extends DateTimeSupport {

  /** UK tax year starts on 6th of April, and ends on the following 5th of April
    */
  private val taxYearMonthStartEnd = 4
  private val taxYearDayStart      = 6

  private val tysEndYearRegex   = """\d{2}-(\d{2})""".r
  private val yyyyEndYearRegex  = """(\d{4})""".r
  private val mtdStartYearRegex = """(\d{4})-\d{2}""".r

  def ending(year: Int): TaxYear = new TaxYear(year) {}

  /*
   * Gets a TaxYear from YY-YY (e.g. 22-23) format
   */
  def fromTys(value: String): TaxYear =
    value match {
      case tysEndYearRegex(endYear) => TaxYear.ending(2000 + endYear.toInt)
      case _ =>
        throw new IllegalArgumentException(s"Invalid value: $value does not match the YY-YY pattern")
    }

  /*
   * Gets a TaxYear from YYYY (e.g. 2023) format
   */
  def fromDownstream(value: String): TaxYear =
    value match {
      case yyyyEndYearRegex(endYear) => TaxYear.ending(endYear.toInt)
      case _ =>
        throw new IllegalArgumentException(s"Invalid value: $value does not match the YYYY pattern")
    }

  /*
   * Gets a TaxYear from YYYY-YY (e.g. 2023-24) format
   */
  def fromMtd(value: String): TaxYear =
    value match {
      case mtdStartYearRegex(startYear) => TaxYear.ending(startYear.toInt + 1)
      case _ =>
        throw new IllegalArgumentException(s"Invalid value: $value does not match the YYYY-YY pattern")
    }

  def containing(date: LocalDate): TaxYear = {
    val endYear = if (date < startInYear(date.getYear)) date.getYear else date.getYear + 1

    TaxYear.ending(endYear)
  }

  private def startInYear(year: Int): LocalDate =
    LocalDate.of(year, taxYearMonthStartEnd, taxYearDayStart)

}

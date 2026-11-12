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

package api.models.domain

import play.api.libs.json.Writes

import java.time.{Clock, LocalDate}
import scala.math.Ordering.Implicits.*

/** Opaque representation of a tax year.
  *
  * @param value
  *   A single-year representation, e.g. "2027" represents the tax year 2026-27.
  */
final case class TaxYear private (private val value: String) {

  /** The year that the tax year ends as a number, e.g. for "2026-27" this will be 2027.
    */
  val year: Int = value.toInt

  /** The year that the tax year starts as a number, e.g. for "2026-27" this will be 2026.
    */
  val startYear: Int = year - 1

  def startDate: LocalDate = TaxYear.startInYear(startYear)

  def endDate: LocalDate = startDate.plusYears(1).minusDays(1)

  /** The tax year in MTD (vendor-facing) format, e.g. "2026-27".
    */
  val asMtd: String = {
    val prefix  = value.take(2)
    val yearTwo = value.drop(2)
    val yearOne = (yearTwo.toInt - 1).toString
    prefix + yearOne + "-" + yearTwo
  }

  /** The tax year in the pre-TYS downstream format, e.g. "2027".
    */
  val asDownstream: String = value

  /** The tax year in the Tax Year Specific downstream format, e.g. "26-27".
    */
  val asTysDownstream: String = {
    val yearTwo = value.toInt - 2000
    val yearOne = yearTwo - 1
    s"$yearOne-$yearTwo"
  }

  override def toString: String = s"TaxYear($value)"
}

object TaxYear {

  /** UK tax year starts on 6 April.
    */
  private val taxYearMonthStart = 4
  private val taxYearDayStart   = 6

  def starting(year: Int): TaxYear = TaxYear.ending(year + 1)
  def ending(year: Int): TaxYear   = new TaxYear(year.toString)

  /** @param taxYear
    *   tax year in MTD format (e.g. 2026-27)
    */
  def fromMtd(taxYear: String): TaxYear =
    TaxYear(taxYear.take(2) + taxYear.drop(5))

  def now(implicit clock: Clock = Clock.systemUTC): TaxYear            = TaxYear.containing(LocalDate.now(clock))
  def currentTaxYear(implicit clock: Clock = Clock.systemUTC): TaxYear = TaxYear.now

  /** @param date
    *   the date in extended ISO-8601 format (e.g. 2027-04-05)
    */
  def fromIso(date: String): TaxYear = containing(LocalDate.parse(date))

  def containing(date: LocalDate): TaxYear = {
    val year = (
      if (isPreviousTaxYear(date)) date.getYear else date.getYear + 1
    ).toString

    new TaxYear(year)
  }

  private def isPreviousTaxYear(date: LocalDate): Boolean = {
    val taxYearStartDate = LocalDate.of(date.getYear, taxYearMonthStart, taxYearDayStart)
    date.isBefore(taxYearStartDate)
  }

  private def startInYear(year: Int): LocalDate = LocalDate.of(year, taxYearMonthStart, taxYearDayStart)

  def fromDownstream(taxYear: String): TaxYear = new TaxYear(taxYear)

  implicit val ordering: Ordering[TaxYear] = Ordering.by(_.year)

  implicit val writes: Writes[TaxYear] = implicitly[Writes[String]].contramap(_.asMtd)
}

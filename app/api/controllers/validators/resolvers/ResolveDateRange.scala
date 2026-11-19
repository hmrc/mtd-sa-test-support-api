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
import api.models.errors.{EndDateFormatError, MtdError, RuleEndBeforeStartDateError, StartDateFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.*

import java.time.LocalDate
import scala.math.Ordering.Implicits.infixOrderingOps

case class ResolveDateRange(startDateFormatError: MtdError = StartDateFormatError,
                            endDateFormatError: MtdError = EndDateFormatError,
                            endBeforeStartDateError: MtdError = RuleEndBeforeStartDateError)
    extends ResolverSupport {

  val resolver: Resolver[(String, String), DateRange] = { (startDate, endDate) =>
    (
      ResolveIsoDate(startDate, startDateFormatError),
      ResolveIsoDate(endDate, endDateFormatError)
    ).mapN(resolveDateRange).andThen(identity)
  }

  private val optionalResolver: Resolver[(Option[String], Option[String]), (Option[LocalDate], Option[LocalDate])] = { (startDate, endDate) =>
    (
      ResolveIsoDate(startDate, startDateFormatError),
      ResolveIsoDate(endDate, endDateFormatError)
    ).mapN(resolveDateRangeOptionally).andThen(identity)
  }

  def apply(value: (String, String)): Validated[Seq[MtdError], DateRange] = resolver(value)

  def apply(start: Option[String], end: Option[String]): Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] =
    optionalResolver(start, end)

  private def resolveDateRange(parsedStartDate: LocalDate, parsedEndDate: LocalDate): Validated[Seq[MtdError], DateRange] =
    if (parsedEndDate < parsedStartDate) Invalid(List(endBeforeStartDateError)) else Valid(DateRange(parsedStartDate, parsedEndDate))

  private def resolveDateRangeOptionally(parsedStartDate: Option[LocalDate],
                                         parsedEndDate: Option[LocalDate]): Validated[Seq[MtdError], (Option[LocalDate], Option[LocalDate])] = {
    (parsedStartDate, parsedEndDate) match {
      case (Some(start), Some(end)) if end < start => Invalid(List(endBeforeStartDateError))
      case _                                       => Valid(parsedStartDate, parsedEndDate)
    }
  }

}

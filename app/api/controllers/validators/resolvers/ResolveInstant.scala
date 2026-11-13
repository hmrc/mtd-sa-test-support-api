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

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

/** Parses a timestamp using the supplied formatter, defaulting to the format YYYY-MM-DDThh:mm:ss.SSSZ, and returns a new Instant.
  */
object ResolveInstant {

  private val defaultFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

  def apply(value: String, error: => MtdError, formatter: DateTimeFormatter = defaultFormatter): Validated[Seq[MtdError], Instant] =
    Try(Instant.from(formatter.parse(value))) match {
      case Success(instant) => Valid(instant)
      case Failure(_)       => Invalid(List(error))
    }

  def apply(value: Option[String], error: => MtdError, formatter: DateTimeFormatter): Validated[Seq[MtdError], Option[Instant]] =
    value match {
      case Some(value) => apply(value, error, formatter).map(Option(_))
      case None        => Valid(None)
    }

  def apply(value: Option[String], error: => MtdError): Validated[Seq[MtdError], Option[Instant]] = apply(value, error, defaultFormatter)

}

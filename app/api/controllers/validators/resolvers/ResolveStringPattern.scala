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

import scala.util.matching.Regex

object ResolveStringPattern {

  def apply(value: String, regexFormat: Regex, error: => MtdError): Validated[Seq[MtdError], String] =
    if (value.trim.nonEmpty && regexFormat.matches(value)) Valid(value) else Invalid(List(error))

  def apply(value: Option[String], regexFormat: Regex, error: => MtdError): Validated[Seq[MtdError], Option[String]] =
    value match {
      case Some(value) => apply(value, regexFormat, error).map(Option(_))
      case None        => Valid(None)
    }

}

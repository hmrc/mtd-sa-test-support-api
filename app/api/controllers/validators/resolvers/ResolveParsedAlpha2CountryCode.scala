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

import api.models.errors.*
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import com.neovisionaries.i18n.CountryCode

object ResolveParsedAlpha2CountryCode {

  // These are in the API#1171 spec but not supported by getByAlpha2Code
  private[resolvers] val permittedCustomCodes = Set("FC", "OR", "UN", "ZZ")

  def apply(value: String, path: String): Validated[Seq[MtdError], String] =
    if (value.length != 2) {
      Invalid(List(CountryCodeFormatError.withPath(path)))
    } else if (permittedCustomCodes.contains(value)) {
      Valid(value)
    } else {
      Option(CountryCode.getByAlpha2Code(value)) match {
        case Some(_) => Valid(value)
        case None    => Invalid(List(CountryCodeFormatError.forRuleCountryCode.withPath(path)))
      }
    }

  def apply(maybeValue: Option[String], path: String): Validated[Seq[MtdError], Option[String]] =
    maybeValue match {
      case Some(value) => apply(value, path).map(Option(_))
      case None        => Valid(None)
    }

}

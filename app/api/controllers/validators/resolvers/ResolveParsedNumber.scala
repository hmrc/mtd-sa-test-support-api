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

import api.models.errors.{MtdError, ValueFormatError}
import cats.data.Validated

case class ResolveParsedNumber(min: BigDecimal = 0, max: BigDecimal = 99999999999.99, disallowZero: Boolean = false) extends ResolverSupport {

  def resolver(error: => MtdError): Resolver[BigDecimal, BigDecimal] =
    resolveValid[BigDecimal].thenValidate(validator(error))

  def validator(error: => MtdError): Validator[BigDecimal] = { (value: BigDecimal) =>
    val valid = min <= value && value <= max && value.scale <= 2 && (!disallowZero || value != 0)

    Option.when(!valid)(List(error))
  }

  def apply(value: Option[BigDecimal], path: String): Validated[Seq[MtdError], Option[BigDecimal]] =
    resolver(errorFor(path)).resolveOptionally(value)

  def apply(value: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    resolver(errorFor(path))(value)

  def apply(value: Option[BigDecimal], error: => MtdError): Validated[Seq[MtdError], Option[BigDecimal]] =
    resolver(error).resolveOptionally(value)

  def apply(value: BigDecimal, error: => MtdError): Validated[Seq[MtdError], BigDecimal] =
    resolver(error)(value)

  private def errorFor(path: String) = ValueFormatError.forPathAndRange(path, min.toString, max.toString)

}

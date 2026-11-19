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
import cats.implicits.*

import scala.math.Ordered.orderingToOrdered

/** Provides utilities and extension methods for resolvers and validators.
  */
trait ResolverSupport {
  type Resolver[In, Out] = In => Validated[Seq[MtdError], Out]
  type Validator[A]      = A => Option[Seq[MtdError]]

  implicit class ResolverOps[In, Out](resolver: In => Validated[Seq[MtdError], Out]) {
    def map[Out2](f: Out => Out2): Resolver[In, Out2] = i => resolver(i).map(f)

    def thenResolve[Out2](other: Resolver[Out, Out2]): Resolver[In, Out2] = in => resolver(in).andThen(other)

    def thenValidate(validator: Validator[Out]): Resolver[In, Out] = i => resolver(i).andThen(o => validator(o).toInvalid(o))

    def resolveOptionally: Resolver[Option[In], Option[Out]] = _.map(in => resolver(in).map(Some(_))).getOrElse(Valid(None))

    def resolveOptionallyWithDefault(default: => Out): Resolver[Option[In], Out] = _.map(in => resolver(in)).getOrElse(Valid(default))

    def asValidator: Validator[In] = in =>
      resolver(in) match {
        case Valid(_)      => None
        case Invalid(errs) => Some(errs)
      }

  }

  implicit class ValidatorOps[A](validator: A => Option[Seq[MtdError]]) {
    def thenValidate(other: Validator[A]): Validator[A] = a => validator(a).orElse(other(a))

    def contramap[B](f: B => A): Validator[B] = b => validator(f(b))

    def validateOptionally: Validator[Option[A]] = _.flatMap(validator)
  }

  /** Use to lift a Validator to a Resolver that validates. E.g.
    * {{{
    * resolveValid[Int] thenValidate satisfiesMax(1000, someError)
    * }}}
    */
  def resolveValid[A]: Resolver[A, A] = a => Valid(a)

  def resolvePartialFunction[A, B](error: => MtdError)(pf: PartialFunction[A, B]): Resolver[A, B] =
    a => pf.map(Valid(_)).applyOrElse(a, (_: A) => Invalid(List(error)))

  def satisfies[A](error: => MtdError)(predicate: A => Boolean): Validator[A] =
    a => Option.when(!predicate(a))(List(error))

  def inRange[A: Ordering](minAllowed: A, maxAllowed: A, error: => MtdError): Validator[A] =
    satisfiesMin[A](minAllowed, error).thenValidate(satisfiesMax[A](maxAllowed, error))

  def satisfiesMin[A: Ordering](minAllowed: A, error: => MtdError): Validator[A] = satisfies(error)(minAllowed <= _)
  def satisfiesMax[A: Ordering](maxAllowed: A, error: => MtdError): Validator[A] = satisfies(error)(_ <= maxAllowed)

  def combinedValidator[A](first: Validator[A], others: Validator[A]*): Validator[A] = { (value: A) =>
    val validators = first +: others

    val validations = validators.map(validator => validator(value))

    validations.reduce(_ combine _)
  }

}

/** To allow an import-based alternative to extension */
object ResolverSupport extends ResolverSupport

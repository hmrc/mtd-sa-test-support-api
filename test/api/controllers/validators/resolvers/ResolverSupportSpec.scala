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
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxOption

import java.util.concurrent.atomic.AtomicInteger

class ResolverSupportSpec extends UnitSpec with ResolverSupport {
  private val notIntegerError = MtdError("NOT_INT", "Not integer", 400)
  private val outOfRangeError = MtdError("OUT_OF_RANGE", "Out of range", 400)
  private val oddNumberError  = MtdError("ODD", "Odd", 400)
  private val notPresentError = MtdError("NOT_PRESENT", "Absent", 400)

  private val resolveInt: Resolver[String, Int] = _.toIntOption.toValid(List(notIntegerError))

  "ResolverSupport" should {
    "provide the ability to easily create predicate based validators" in {
      val validator = satisfies[Int](outOfRangeError)(_ < 10)

      validator(1) shouldBe None
      validator(10) shouldBe Some(List(outOfRangeError))
    }

    "provide the ability to easily create minimum allowed based validators" in {
      val validator = satisfiesMin[Int](10, outOfRangeError)

      validator(9) shouldBe Some(List(outOfRangeError))
      validator(10) shouldBe None
      validator(11) shouldBe None
    }

    "provide the ability to easily create maximum allowed based validators" in {
      val validator = satisfiesMax[Int](10, outOfRangeError)

      validator(9) shouldBe None
      validator(10) shouldBe None
      validator(11) shouldBe Some(List(outOfRangeError))
    }

    "provide the ability to easily create validators based on a min max range" in {
      val validator = inRange[Int](8, 10, outOfRangeError)

      validator(7) shouldBe Some(List(outOfRangeError))
      validator(9) shouldBe None
      validator(10) shouldBe None
      validator(11) shouldBe Some(List(outOfRangeError))
    }

    "provide the ability to compose a resolver with a subsequent validator" in {
      val resolver = resolveInt.thenValidate(satisfiesMax(9, outOfRangeError))

      resolver("9") shouldBe Valid(9)
      resolver("10") shouldBe Invalid(List(outOfRangeError))
      resolver("xx") shouldBe Invalid(List(notIntegerError))
    }

    "provide the ability to compose a resolver with multiple subsequent validators (and keep all errors)" in {
      val isEven = satisfies[Int](oddNumberError)(_ % 2 == 0)

      val resolver = resolveInt.thenValidate(combinedValidator(satisfiesMax(10, outOfRangeError), isEven))

      resolver("2") shouldBe Valid(2)
      resolver("3") shouldBe Invalid(List(oddNumberError))
      resolver("10") shouldBe Valid(10)
      resolver("11") shouldBe Invalid(List(outOfRangeError, oddNumberError))
      resolver("12") shouldBe Invalid(List(outOfRangeError))
      resolver("xx") shouldBe Invalid(List(notIntegerError))
    }

    "provide the ability to map a valid result" in {
      val resolver = resolveInt.map(v => -v)

      resolver("2") shouldBe Valid(-2)
      resolver("xx") shouldBe Invalid(List(notIntegerError))
    }

    "provide the ability to validate against an optional value" in {
      val resolver = resolveInt.resolveOptionally

      resolver(Some("1")) shouldBe Valid(Some(1))
      resolver(Some("xx")) shouldBe Invalid(List(notIntegerError))
      resolver(None) shouldBe Valid(None)
    }

    "provide the ability to validate against an optional value with a default" in {
      val resolver = resolveInt.resolveOptionallyWithDefault(0)

      resolver(Some("1")) shouldBe Valid(1)
      resolver(Some("xx")) shouldBe Invalid(List(notIntegerError))
      resolver(None) shouldBe Valid(0)
    }

    "provide the ability to validate against an optional value with a default that is determined each time it is used" in {
      val next              = new AtomicInteger(0)
      def defaultValue: Int = next.getAndIncrement()

      val resolver = resolveInt.resolveOptionallyWithDefault(defaultValue)

      resolver(Some("1")) shouldBe Valid(1)
      resolver(Some("xx")) shouldBe Invalid(List(notIntegerError))
      resolver(None) shouldBe Valid(0)
      resolver(None) shouldBe Valid(1)
      resolver(None) shouldBe Valid(2)
    }

    "provides the ability to create resolvers from partial functions" in {
      val resolver = resolvePartialFunction[Int, String](outOfRangeError) {
        case i if i <= 10 => i.toString
      }

      resolver(1) shouldBe Valid("1")
      resolver(11) shouldBe Invalid(List(outOfRangeError))
    }

    "provides the ability to create resolvers don't actually parse but only validate" in {
      val resolver = resolveValid[Int].thenValidate(satisfiesMax(10, outOfRangeError))

      resolver(1) shouldBe Valid(1)
      resolver(11) shouldBe Invalid(List(outOfRangeError))
    }

    "provides the ability to create validators out of a resolver (throwing away any result)" in {
      val validator = resolveInt.asValidator

      validator("1") shouldBe None
      validator("XX") shouldBe Some(List(notIntegerError))
    }

    "provides the ability to combine resolvers with thenResolve" in {
      val resolvePresent: Resolver[Option[Int], Int] = _.toValid(Seq(notPresentError))
      val resolver                                   = resolveInt.resolveOptionally.thenResolve(resolvePresent)

      resolver(Some("1")) shouldBe Valid(1)
      resolver(Some("XX")) shouldBe Invalid(List(notIntegerError))
    }

    "provides the ability to create a validator for a larger object based on validators of its parts" in {
      val partValidator = satisfiesMax(10, outOfRangeError)

      // Validator for tuples (String, Int) that validates the Int part
      val resolver = resolveValid[(String, Int)].thenValidate(partValidator.contramap(_._2))

      resolver(("A", 1)) shouldBe Valid(("A", 1))
      resolver(("A", 11)) shouldBe Invalid(List(outOfRangeError))
    }

    "provides the ability to create a validator for a larger object based on validators of its optional parts" in {
      val partValidator = satisfiesMax(10, outOfRangeError)

      // Validator for tuples (String, Option[Int]) that validates the Int part
      val resolver = resolveValid[(String, Option[Int])].thenValidate(partValidator.validateOptionally.contramap(_._2))

      resolver(("A", None)) shouldBe Valid(("A", None))
      resolver(("A", Some(1))) shouldBe Valid(("A", Some(1)))
      resolver(("A", Some(11))) shouldBe Invalid(List(outOfRangeError))
    }
  }

}

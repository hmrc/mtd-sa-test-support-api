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

package api.utils

import api.utils.EmptinessChecker.*
import api.utils.EmptyPathsResult.*

class EmptinessCheckerSpec extends UnitSpec {

  sealed trait SomeEnum

  object SomeEnum {
    case object E1 extends SomeEnum
    case object E2 extends SomeEnum

    implicit val ckr: EmptinessChecker[SomeEnum] = EmptinessChecker.primitive
  }

  case class Baz(a: Option[Int] = None, e: Option[SomeEnum] = None)
  case class Bar(baz: Option[Baz] = None, arr: Option[List[Bar]] = None)

  case class Foo(bar: Option[Bar] = None,
                 arr1: Option[List[Bar]] = None,
                 arr2: Option[List[Bar]] = None,
                 arr3: Option[List[Bar]] = None,
                 bar2: Option[Bar] = None)

  "EmptinessChecker" when {
    "empty object" should {
      "return root path as empty" in {
        val result = EmptinessChecker.findEmptyPaths(Foo())

        result shouldBe CompletelyEmpty
      }
    }

    "all arrays and objects are non empty" should {
      "return empty" in {
        val barFull = Bar(baz = Some(Baz(Some(1))))
        val result  = EmptinessChecker.findEmptyPaths(Foo(bar = Some(barFull), arr1 = Some(List(barFull))))

        result shouldBe NoEmptyPaths
      }
    }

    "has an empty object" should {
      "return a path" in {
        val result = EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar())))

        result shouldBe EmptyPaths(List("/bar"))
      }
    }

    "has empty object nested" should {
      "return the path" in {
        val result = EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar(Some(Baz())))))

        result shouldBe EmptyPaths(List("/bar/baz"))
      }
    }

    "has empty list" should {
      "return the path of the list" in {
        val result = EmptinessChecker.findEmptyPaths(Foo(arr1 = Some(List())))

        result shouldBe EmptyPaths(List("/arr1"))
      }
    }

    "has empty list nested" should {
      "return the path of the list" in {
        val result = EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar(arr = Some(List())))))

        result shouldBe EmptyPaths(List("/bar/arr"))
      }
    }

    "has empty objects inside list" should {
      "return the paths" in {
        val result = EmptinessChecker.findEmptyPaths(Foo(arr2 = Some(List(Bar(Some(Baz())), Bar(Some(Baz()))))))

        result shouldBe EmptyPaths(List("/arr2/0/baz", "/arr2/1/baz"))
      }
    }

    "has multiple empty objects" should {
      "return an error with the paths for all of them" in {
        val result = EmptinessChecker.findEmptyPaths(
          Foo(bar = Some(Bar(Some(Baz()))), arr1 = Some(Nil), arr2 = Some(List(Bar())), arr3 = Some(List(Bar(Some(Baz())))), bar2 = Some(Bar())))

        result shouldBe EmptyPaths(List("/bar/baz", "/arr1", "/arr2/0", "/arr3/0/baz", "/bar2"))
      }
    }

    "given primitives, Options, Seq and List with no emptiness" should {
      "return NoEmptyPaths" in {
        EmptinessChecker[String].findEmptyPaths("test") shouldBe NoEmptyPaths
        EmptinessChecker[Int].findEmptyPaths(1) shouldBe NoEmptyPaths
        EmptinessChecker[Double].findEmptyPaths(1.00) shouldBe NoEmptyPaths
        EmptinessChecker[Boolean].findEmptyPaths(true) shouldBe NoEmptyPaths
        EmptinessChecker[BigInt].findEmptyPaths(BigInt(1)) shouldBe NoEmptyPaths
        EmptinessChecker[BigDecimal].findEmptyPaths(BigDecimal(1)) shouldBe NoEmptyPaths
        EmptinessChecker[Option[String]].findEmptyPaths(Some("test")) shouldBe NoEmptyPaths
        EmptinessChecker[Option[String]].findEmptyPaths(None) shouldBe NoEmptyPaths
        EmptinessChecker[List[Int]].findEmptyPaths(List(1, 2)) shouldBe NoEmptyPaths
        EmptinessChecker[Seq[String]].findEmptyPaths(Vector("test", "test")) shouldBe NoEmptyPaths
      }
    }
  }

}

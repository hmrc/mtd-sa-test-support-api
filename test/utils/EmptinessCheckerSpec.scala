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

package utils

import support.UnitSpec
import utils.EmptinessChecker._

import EmptyPathsResult._

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
    "empty object" must {
      "return root path as empty" in {
        EmptinessChecker.findEmptyPaths(Foo()) shouldBe CompletelyEmpty
      }
    }

    "all arrays and objects are non empty" must {
      "return empty" in {
        val barFull = Bar(baz = Some(Baz(Some(1))))
        EmptinessChecker.findEmptyPaths(Foo(bar = Some(barFull), arr1 = Some(List(barFull)))) shouldBe NoEmptyPaths
      }
    }

    "has an empty object" must {
      "return an path" in {
        EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar()))) shouldBe EmptyPaths(List("/bar"))
      }
    }

    "has empty object nested" must {
      "return the path" in {
        EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar(Some(Baz()))))) shouldBe EmptyPaths(List("/bar/baz"))
      }
    }

    "has empty list" must {
      "return the path of the list" in {
        EmptinessChecker.findEmptyPaths(Foo(arr1 = Some(List()))) shouldBe EmptyPaths(List("/arr1"))
      }
    }

    "has empty list nested" must {
      "return the path of the list" in {
        EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar(arr = Some(List()))))) shouldBe EmptyPaths(List("/bar/arr"))
      }
    }

    "has empty objects inside list" must {
      "return the paths" in {
        EmptinessChecker.findEmptyPaths(Foo(arr2 = Some(List(Bar(Some(Baz())), Bar(Some(Baz())))))) shouldBe
          EmptyPaths(List("/arr2/0/baz", "/arr2/1/baz"))
      }
    }

    "has multiple empty objects" must {
      "return an error with the paths for all of them" in {
        EmptinessChecker.findEmptyPaths(Foo(bar = Some(Bar(Some(Baz()))),
                                            arr1 = Some(Nil),
                                            arr2 = Some(List(Bar())),
                                            arr3 = Some(List(Bar(Some(Baz())))),
                                            bar2 = Some(Bar()))) shouldBe
          EmptyPaths(List("/bar/baz", "/arr1", "/arr2/0", "/arr3/0/baz", "/bar2"))
      }
    }
  }
}

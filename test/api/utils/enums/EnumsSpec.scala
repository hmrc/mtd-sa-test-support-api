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

package api.utils.enums

import api.utils.UnitSpec
import cats.Show
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Inspectors
import play.api.libs.json.*

enum Enum {
  case `enum-one`, `enum-two`, `enum-three`
}

object Enum {
  given Format[Enum] = Enums.format(values)
}

case class Foo[A](someField: A)

object Foo {
  given [A: Format]: OFormat[Foo[A]] = Json.format[Foo[A]]
}

class EnumsSpec extends UnitSpec with Inspectors {

  import Enum.*

  given Arbitrary[Enum] = Arbitrary(Gen.oneOf(values.toList))

  "EnumJson" should {

    "check toString assumption" in {
      `enum-two`.toString shouldBe "enum-two"
    }

    def json(value: Enum): JsValue = Json.parse(
      s"""
        |{
        | "someField": "$value"
        |}
      """.stripMargin
    )

    "generates reads" in {
      forAll(values.toList) { value =>
        json(value).as[Foo[Enum]] shouldBe Foo(value)
      }
    }

    "generates writes" in {
      forAll(values.toList) { value =>
        Json.toJson(Foo(value)) shouldBe json(value)
      }
    }

    "read using default Show" in {
      val enumReads: Reads[Enum] = Enums.reads(values)

      enumReads.reads(JsString("enum-one")) shouldBe JsSuccess(`enum-one`)
      enumReads.reads(JsString("unknown")) shouldBe a[JsError]
    }

    "write using default Show" in {
      val enumWrites: Writes[Enum] = Enums.writes[Enum]

      enumWrites.writes(`enum-two`) shouldBe JsString("enum-two")
    }

    "allow roundtrip" in {
      forAll(values.toList) { value =>
        val foo: Foo[Enum] = Foo(value)
        Json.toJson(foo).as[Foo[Enum]] shouldBe foo
      }
    }

    "allows external parse by name" in {
      Enums.parser(values).lift("enum-one") shouldBe Some(`enum-one`)
      Enums.parser(values).lift("unknown") shouldBe None
    }

    "allows alternative names (specified by method)" in {

      enum Enum2(val altName: String) {
        case `enum-one`   extends Enum2("one")
        case `enum-two`   extends Enum2("two")
        case `enum-three` extends Enum2("three")
      }

      object Enum2 {
        given Show[Enum2] = Show.show[Enum2](_.altName)

        given Format[Enum2] = Enums.format(values)
      }

      import Enum2.*

      val json: JsValue = Json.parse(
        """
          |{
          | "someField": "one"
          |}
        """.stripMargin
      )

      json.as[Foo[Enum2]] shouldBe Foo(`enum-one`)
      Json.toJson(Foo[Enum2](`enum-one`)) shouldBe json
    }

    "detects badly formatted values" in {
      val badJson: JsValue = Json.parse(
        """
          |{
          | "someField": "unknown"
          |}
        """.stripMargin
      )

      badJson.validate[Foo[Enum]] shouldBe JsError(__ \ "someField", JsonValidationError("error.expected.Enum"))
    }

    "detects type errors" in {
      val badJson: JsValue = Json.parse(
        """
          |{
          | "someField": 123
          |}
        """.stripMargin
      )

      badJson.validate[Foo[Enum]] shouldBe JsError(__ \ "someField", JsonValidationError("error.expected.jsstring"))
    }

    "only work for enums with singleton cases (no parameters)" in {
      assertTypeError(
        """
          |      enum NotEnum {
          |         case ObjectOne
          |         case CaseClassTwo(value: String)
          |      }
          |
          |      Enums.format(NotEnum.values)
        """.stripMargin
      )
    }
  }

}

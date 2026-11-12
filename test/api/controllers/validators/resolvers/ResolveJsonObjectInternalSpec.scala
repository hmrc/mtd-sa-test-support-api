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

import api.models.errors.RuleIncorrectOrEmptyBodyError
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.*

class ResolveJsonObjectInternalSpec extends UnitSpec with JsonErrorValidators {

  case class Bar(field1: String, field2: String)

  case class Foo(bar: Bar, bars: Seq[Bar])

  implicit val barReads: Reads[Bar] = Json.reads
  implicit val fooReads: Reads[Foo] = Json.reads

  private val resolver = ResolveJsonObjectInternal.resolver[Foo]

  "ResolveJsonObjectInternal" when {
    "the JSON is valid" should {
      "return the JSON and output object" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "bars": []}""")

        resolver(json) shouldBe Valid((json, Foo(bar = Bar("field one", "field two"), Nil)))
      }
    }

    "an empty object is passed" should {
      "return an RuleIncorrectOrEmptyBodyError" in {
        resolver(JsObject.empty) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }
    }

    "a string is passed" should {
      "return an RuleIncorrectOrEmptyBodyError" in {
        resolver(JsString("someString")) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }
    }

    "a boolean is passed" should {
      "return an RuleIncorrectOrEmptyBodyError" in {
        resolver(JsBoolean(true)) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }
    }

    "an array is passed" should {
      "return an RuleIncorrectOrEmptyBodyError" in {
        resolver(JsArray(Seq.empty)) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }
    }

    "an Null is passed" should {
      "return an RuleIncorrectOrEmptyBodyError" in {
        resolver(JsNull) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }
    }

    "required fields are missing" when {
      "the field is at the top level" should {
        "return an error with path to the missing field" in {
          val json = Json.parse("""{ "bars": []}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar")))
        }
      }

      "the field is nested in an object" should {
        "return an error with path to the missing field" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one" }, "bars": []}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar/field2")))
        }
      }

      "the field is nested in an object in an array" should {
        "return an error with path to the missing field" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "bars": [{"field1" : "field one" }]}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bars/0/field2")))
        }
      }

      "multiple fields are missing" should {
        "return an error with paths to the missing fields" in {
          val json = Json.parse("""{ "bar": {}, "bars": []}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPaths(Seq("/bar/field1", "/bar/field2"))))
        }
      }
    }

    "fields are the wrong type" should {
      "the field is at the top level" should {
        "return an error with path to the bad field" in {
          val json = Json.parse("""{ "bar": true, "bars": []}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar")))
        }
      }

      "the field is nested in an object" should {
        "return an error with path to the bad field" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one", "field2": 2 }, "bars": []}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar/field2")))
        }
      }

      "the field is nested in an object in an array" should {
        "return an error with path to the bad field" in {
          val json =
            Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "bars": [{"field1" : "field one", "field2": 2 }]}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bars/0/field2")))
        }
      }

      "multiple fields are the wrong type" should {
        "return an error with paths to the bad fields" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "bars": [{"field1" : 2, "field2": 2 }]}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPaths(Seq("/bars/0/field1", "/bars/0/field2"))))
        }
      }
    }

    "multiple types of errors are present" should {
      "the field is at the top level" should {
        "return an error with paths to all problematic fields" in {
          val json = Json.parse("""{  "bars": [{"field1" : "field one", "field2": 2 }]}""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPaths(Seq("/bar", "/bars/0/field2"))))
        }
      }
    }

    "other JSON validation errors are present" should {
      "return an error with the path to the problematic field" in {
        val customReads: Reads[Foo] = Reads(_ => JsError(__ \ "bar" \ "field1", "custom.validation.error"))

        val customResolver = ResolveJsonObjectInternal[Foo]()(customReads).resolver
        val json           = Json.parse("""{ "bar": {}, "bars": [] }""")

        customResolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar/field1")))
      }
    }

  }

}

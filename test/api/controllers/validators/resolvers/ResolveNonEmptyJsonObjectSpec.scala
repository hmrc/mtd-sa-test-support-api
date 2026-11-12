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
import api.utils.EmptinessChecker.field
import api.utils.{EmptinessChecker, UnitSpec}
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.*

class ResolveNonEmptyJsonObjectSpec extends UnitSpec with ResolverSupport with JsonErrorValidators {

  case class Bar(field1: String, field2: String)
  case class Baz(field1: Option[String], field2: Option[String])
  case class Qux(mandatory: String, oneOf1: Option[String] = None, oneOf2: Option[String] = None)

  case class EmptyFoo(field1: Option[String] = None, field2: Option[String] = None)

  // at least one of oneOf1 and oneOf2 must be included:
  implicit val emptinessChecker: EmptinessChecker[Qux] = EmptinessChecker.use { o =>
    List(
      field("oneOf1", o.oneOf1),
      field("oneOf2", o.oneOf2)
    )
  }

  case class Foo(bar: Bar, bars: Option[Seq[Bar]] = None, baz: Option[Baz] = None, qux: Option[Qux] = None)

  implicit val barReads: Reads[Bar]           = Json.reads
  implicit val bazReads: Reads[Baz]           = Json.reads
  implicit val quxReads: Reads[Qux]           = Json.reads
  implicit val emptyFooReads: Reads[EmptyFoo] = Json.reads
  implicit val fooReads: Reads[Foo]           = Json.reads

  private def jsonObjectResolver(resolver: Resolver[JsValue, Foo]): Unit = {
    "return the parsed object" when {
      "given a valid JSON object" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }}""")

        resolver(json) shouldBe Valid(Foo(bar = Bar("field one", "field two")))
      }
    }

    "return the expected error " when {
      "a required field is missing" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one" } }""")

        resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar/field2")))
      }
    }
  }

  private def jsonObjectResolverWithEmptinessChecking(resolver: Resolver[JsValue, Foo]): Unit = {

    "given an empty JSON object at the top level" in {
      resolver(JsObject.empty) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
    }

    "detect empty objects" in {
      val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "baz": {} }""")

      resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/baz")))
    }

    "detect empty arrays" in {
      val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "bars": [] }""")

      resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bars")))
    }

    "allow custom emptiness checker requiring at least one of a number of optional fields" when {
      "one of the optional fields is present" should {
        "return the object" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "qux": { "mandatory": "m", "oneOf1": "a1" } }""")

          resolver(json) shouldBe Valid(Foo(bar = Bar("field one", "field two"), qux = Some(Qux("m", oneOf1 = Some("a1")))))
        }
      }

      "none of the required optional fields is present" should {
        "detect this" in {
          val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "qux": { "mandatory": "m" } }""")

          resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/qux")))
        }
      }
    }
  }

  "ResolveNonEmptyJsonObject" when {

    "the default resolver is used" should {
      val resolver = ResolveNonEmptyJsonObject.resolver[Foo]

      behave like jsonObjectResolver(resolver)
      behave like jsonObjectResolverWithEmptinessChecking(resolver)

      "detect a non-empty JSON object with no expected fields" in {
        val resolver = ResolveNonEmptyJsonObject.resolver[EmptyFoo]
        val json     = Json.parse("""{ "unexpected": "value" }""")

        resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }

      "not detect extra fields in the input" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two", "extra": "x" }}""")

        resolver(json) shouldBe Valid(Foo(bar = Bar("field one", "field two")))
      }
    }

    "an instance is created" should {
      "delegate to the companion object resolver and apply" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }}""")

        val instance = ResolveNonEmptyJsonObject[Foo]

        instance.resolver(json) shouldBe Valid(Foo(bar = Bar("field one", "field two")))
        instance(json) shouldBe Valid(Foo(bar = Bar("field one", "field two")))
      }
    }

    "the strict resolver is used" should {
      val resolver = ResolveNonEmptyJsonObject.strictResolver[Foo]

      behave like jsonObjectResolver(resolver)
      behave like jsonObjectResolverWithEmptinessChecking(resolver)

      "detect extra fields in the input" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two", "extra": "x" }}""")

        resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/bar/extra")))
      }

      "detect extra fields first (when object would otherwise be detected as empty)" in {
        val json = Json.parse("""{ "bar": {"field1" : "field one", "field2" : "field two" }, "baz": { "extra": "x" } }""")

        resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/baz/extra")))
      }
    }
  }

}

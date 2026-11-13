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
import play.api.libs.json.{JsValue, Json, Reads}

class ResolveJsonObjectSpec extends UnitSpec with ResolverSupport with JsonErrorValidators {

  case class Foo(field1: String, field2: String)

  implicit val fooReads: Reads[Foo] = Json.reads

  private def jsonObjectResolver(resolver: Resolver[JsValue, Foo]): Unit = {
    "return the parsed object" when {
      "given a valid JSON object" in {
        val json = Json.parse("""{ "field1" : "field one", "field2" : "field two" }""")

        resolver(json) shouldBe Valid(Foo("field one", "field two"))
      }
    }

    "return the expected error " when {
      "a required field is missing" in {
        val json = Json.parse("""{ "field1" : "field one" }""")

        resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/field2")))
      }
    }
  }

  "ResolveJsonObject" when {
    "the default resolver is used" should {
      val resolver = ResolveJsonObject.resolver[Foo]

      behave like jsonObjectResolver(resolver)

      "not detect extra fields in the input" in {
        val json = Json.parse("""{ "extra":123 , "field1" : "field one", "field2" : "field two" }""")

        resolver(json) shouldBe Valid(Foo("field one", "field two"))
      }
    }
  }

  "the strict resolver is used" should {
    val resolver = ResolveJsonObject.strictResolver[Foo]

    behave like jsonObjectResolver(resolver)

    "detect extra fields in the input" in {
      val json = Json.parse("""{ "extra":123 , "field1" : "field one", "field2" : "field two" }""")

      resolver(json) shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError.withPath("/extra")))
    }
  }

}

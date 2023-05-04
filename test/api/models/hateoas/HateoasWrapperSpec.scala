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

package api.models.hateoas

import api.models.hateoas.Method.GET
import play.api.libs.json.{Json, OWrites}
import support.UnitSpec

class HateoasWrapperSpec extends UnitSpec {

  case class TestMtdResponse(field1: String, field2: Int)

  object TestMtdResponse {
    implicit val writes: OWrites[TestMtdResponse] = Json.writes[TestMtdResponse]
  }

  "HateoasWrapper writes" must {
    "place links alongside wrapped object fields" in {
      Json.toJson(HateoasWrapper(TestMtdResponse("value1", 123), Seq(Link("/some/resource", GET, "thing")))) shouldBe
        Json.parse("""
      |{
      |"field1": "value1",
      |"field2": 123,
      |"links" : [
      |  {
      |    "href": "/some/resource",
      |    "rel": "thing",
      |    "method": "GET"
      |  }
      | ]
      |}
      """.stripMargin)
    }

    "not write links array if there are no links" in {
      Json.toJson(HateoasWrapper(TestMtdResponse("value1", 123), Nil)) shouldBe
        Json.parse("""
                     |{
                     |"field1": "value1",
                     |"field2": 123
                     |}
    """.stripMargin)
    }
  }

  "HateoasWrapper writesEmpty" must {
    "write links" in {
      Json.toJson(HateoasWrapper((), Seq(Link("/some/resource", GET, "thing")))) shouldBe
        Json.parse("""
                     |{
                     |  "links" : [
                     |    {
                     |      "href": "/some/resource",
                     |      "rel": "thing",
                     |      "method": "GET"
                     |    }
                     |  ]
                     |}
      """.stripMargin)
    }

    "not write links array if there are no links" in {
      Json.toJson(HateoasWrapper((), Nil)) shouldBe
        Json.parse("""{}""".stripMargin)
    }
  }
}

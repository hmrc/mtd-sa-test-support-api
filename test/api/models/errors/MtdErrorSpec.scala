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

package api.models.errors

import api.utils.UnitSpec
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json

class MtdErrorSpec extends UnitSpec {

  private val error = MtdError("CODE", "some message", BAD_REQUEST)

  "writes" should {
    "generate the correct JSON" in {
      Json.toJson(error) shouldBe Json.parse(
        """
          |{
          |   "code": "CODE",
          |   "message": "some message"
          |}
        """.stripMargin
      )
    }
  }

  "maybeWithPath" should {
    "add a path to the error" when {
      "a path is provided" in {
        val result = error.maybeWithPath(Some("path")).paths
        result shouldBe Some(List("path"))
      }
    }
  }

  "maybeWithExtraPath" should {
    "add an extra path to the error" when {
      "a path is provided" in {
        val result = error.maybeWithExtraPath(Some("extra path")).paths
        result shouldBe Some(List("extra path"))
      }
    }
  }

  "withExtraPath" when {
    "paths are undefined" should {
      "create a new error with paths" in {
        val result = error.withExtraPath("aPath")
        result shouldBe error.withPath("aPath")
      }
    }

    "paths are defined" should {
      "add the new path to the existing list of paths" in {
        val dummyErrorWithPaths: MtdError = error.withPath("aPath")

        val result = dummyErrorWithPaths.withExtraPath("aPath2")
        result shouldBe dummyErrorWithPaths.withPaths(List("aPath", "aPath2"))
      }
    }
  }

}

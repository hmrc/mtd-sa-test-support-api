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

package api.models.errors

import play.api.http.Status.{BAD_REQUEST, UNAUTHORIZED}
import play.api.libs.json.Json
import support.UnitSpec

class MtdErrorSpec extends UnitSpec {

  "writes" should {
    "generate the correct JSON" in {
      Json.toJson(MtdError("CODE", "some message", UNAUTHORIZED)) shouldBe Json.parse(
        """
          |{
          |   "code": "CODE",
          |   "message": "some message"
          |}
        """.stripMargin
      )
    }
  }

  "MtdErrorWithCode.unapply" should {
    "return the error code" in {
      CustomMtdError.unapply(MtdError("CODE", "message", BAD_REQUEST)) shouldBe Some("CODE")
    }
  }

}

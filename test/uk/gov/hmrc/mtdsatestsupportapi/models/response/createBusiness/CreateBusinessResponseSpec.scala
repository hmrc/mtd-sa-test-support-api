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

package uk.gov.hmrc.mtdsatestsupportapi.models.response.createBusiness

import play.api.libs.json.Json
import support.UnitSpec

class CreateBusinessResponseSpec extends UnitSpec {

  private val response: CreateBusinessResponse = CreateBusinessResponse("someId")

  "CreateBusinessResponse" when {
    "deserialized from downstream" must {
      "work" in {
        Json.obj("incomeSourceId" -> "someId").as[CreateBusinessResponse] shouldBe response
      }
    }

  }

  "serialized in API response" must {
    "work" in {
      Json.toJson(response) shouldBe Json.obj("businessId" -> "someId")
    }
  }

}

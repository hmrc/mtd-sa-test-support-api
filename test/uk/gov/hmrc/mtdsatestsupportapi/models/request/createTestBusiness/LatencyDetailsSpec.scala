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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness

import api.models.domain.TaxYear
import play.api.libs.json.Json
import support.UnitSpec

class LatencyDetailsSpec extends UnitSpec {

  private val latencyDetails =
    LatencyDetails(
      latencyEndDate = "2020-01-01",
      taxYear1 = TaxYear.fromMtd("2020-21"),
      latencyIndicator1 = LatencyIndicator.A,
      taxYear2 = TaxYear.fromMtd("2021-22"),
      latencyIndicator2 = LatencyIndicator.Q
    )

  "LatencyDetails" when {
    "deserialized from the API JSON" must {
      "work" in {
        val mtdJson = Json.parse("""{
            |    "latencyEndDate": "2020-01-01",
            |    "taxYear1": "2020-21",
            |    "latencyIndicator1": "A",
            |    "taxYear2": "2021-22",
            |    "latencyIndicator2": "Q"
            |}
            |""".stripMargin)

        mtdJson.as[LatencyDetails] shouldBe latencyDetails
      }
    }
  }

  "serialized to downstream JSON" must {
    "work" in {
      Json.toJson(latencyDetails) shouldBe Json.parse("""{
          |    "latencyEndDate": "2020-01-01",
          |    "taxYear1": "2021",
          |    "latencyIndicator1": "A",
          |    "taxYear2": "2022",
          |    "latencyIndicator2": "Q" 
          |}""".stripMargin)
    }
  }

}

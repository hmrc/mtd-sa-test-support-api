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

class QuarterlyTypeChoiceSpec extends UnitSpec {

  private val quarterReportingType = QuarterlyPeriodType.standard
  private val taxYearOfChoice      = TaxYear.fromMtd("2023-24")

  private val model = QuarterlyTypeChoice(quarterReportingType, taxYearOfChoice)

  private val mtdJson = Json.parse("""
      |{
      |   "quarterlyPeriodType": "standard",
      |   "taxYearOfChoice": "2023-24"
      |}
      |""".stripMargin)

  private val downstream = Json.parse(
    """
      |{
      |   "quarterReportingType":"STANDARD",
      |   "taxYearofElection":"2024"
      |}
      |""".stripMargin
  )

  "read" should {
    "work" when {
      "received API JSON" in {
        mtdJson.as[QuarterlyTypeChoice] shouldBe model
      }
    }
  }

  "write" should {
    "write to downstream JSON correctly" in {
      Json.toJson(model) shouldBe downstream
    }
  }

}

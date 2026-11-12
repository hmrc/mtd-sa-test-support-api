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

package v1.createAmendItsaStatus.fixture

import play.api.libs.json.{JsValue, Json}
import v1.createAmendItsaStatus.model.request.*

object CreateAmendItsaStatusFixture {

  val itsaStatusDetailModel: ItsaStatusDetail = ItsaStatusDetail(
    submittedOn = "2024-03-23T16:02:34.039Z",
    status = "MTD Mandated",
    statusReason = "Sign up - return available",
    businessIncome2YearsPrior = Some(4500.23)
  )

  val requestBodyModel: CreateAmendItsaStatusRequestBody = CreateAmendItsaStatusRequestBody(
    itsaStatusDetails = List(itsaStatusDetailModel)
  )

  val mtdItsaStatusDetailJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2024-03-23T16:02:34.039Z",
      |  "status": "MTD Mandated",
      |  "statusReason": "Sign up - return available",
      |  "businessIncome2YearsPrior": 4500.23
      |}
    """.stripMargin
  )

  val mtdRequestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "itsaStatusDetails": [$mtdItsaStatusDetailJson]
      |}
    """.stripMargin
  )

}

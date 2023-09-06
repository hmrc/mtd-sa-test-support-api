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
import api.models.domain.TaxYear.fromMtd
import play.api.libs.json.{Format, Json, Reads, Writes}

case class LatencyDetails(latencyEndDate: String,
                          taxYear1: TaxYear,
                          latencyIndicator1: LatencyIndicator,
                          taxYear2: TaxYear,
                          latencyIndicator2: LatencyIndicator)

object LatencyDetails {

  private implicit val taxYearReads: Reads[TaxYear]   = implicitly[Reads[String]].map(fromMtd)
  private implicit val taxYearWrites: Writes[TaxYear] = implicitly[Writes[String]].contramap(_.asDownstream)

  implicit val format: Format[LatencyDetails] = Json.format
}

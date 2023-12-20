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
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class QuarterlyTypeChoice(quarterlyPeriodType: QuarterlyPeriodType, taxYearOfChoice: TaxYear)

object QuarterlyTypeChoice {
  private implicit val taxYearReads: Reads[TaxYear]   = implicitly[Reads[String]].map(fromMtd)
  private implicit val taxYearWrites: Writes[TaxYear] = implicitly[Writes[String]].contramap(_.asDownstream)

  implicit val reads: Reads[QuarterlyTypeChoice] = Json.reads

  implicit val writes: Writes[QuarterlyTypeChoice] = (
    (JsPath \ "quarterReportingType").write[QuarterlyPeriodType] and
      (JsPath \ "taxYearofElection").write[TaxYear]
    )(unlift(QuarterlyTypeChoice.unapply))


}

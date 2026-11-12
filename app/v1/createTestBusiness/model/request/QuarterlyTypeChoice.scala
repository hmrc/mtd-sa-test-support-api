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

package v1.createTestBusiness.model.request

import api.models.domain.TaxYear
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class QuarterlyTypeChoice(quarterlyPeriodType: String, taxYearOfChoice: String)

object QuarterlyTypeChoice {
  implicit val reads: Reads[QuarterlyTypeChoice] = Json.reads[QuarterlyTypeChoice]

  implicit val writes: OWrites[QuarterlyTypeChoice] = (
    (JsPath \ "quarterReportingType").write[String].contramap[String](QuarterlyPeriodType.parser(_).toDownstream) and
      (JsPath \ "taxYearofElection").write[String].contramap[String](TaxYear.fromMtd(_).asDownstream)
  )(w => Tuple.fromProductTyped(w))

}

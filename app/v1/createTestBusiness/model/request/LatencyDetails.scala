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

case class LatencyDetails(latencyEndDate: String, taxYear1: String, latencyIndicator1: String, taxYear2: String, latencyIndicator2: String)

object LatencyDetails {
  implicit val reads: Reads[LatencyDetails] = Json.reads[LatencyDetails]

  implicit val writes: OWrites[LatencyDetails] = (
    (JsPath \ "latencyEndDate").write[String] and
      (JsPath \ "taxYear1").write[String].contramap[String](TaxYear.fromMtd(_).asDownstream) and
      (JsPath \ "latencyIndicator1").write[String] and
      (JsPath \ "taxYear2").write[String].contramap[String](TaxYear.fromMtd(_).asDownstream) and
      (JsPath \ "latencyIndicator2").write[String]
  )(o => Tuple.fromProductTyped(o))

}

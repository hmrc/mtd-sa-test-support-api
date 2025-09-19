/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}

case class ITSAStatusDetail(submittedOn: String, status: Status, statusReason: StatusReason, businessIncome2YearsPrior: Option[BigDecimal])

object ITSAStatusDetail {

  implicit val reads: Reads[ITSAStatusDetail] = Json.reads[ITSAStatusDetail]

  implicit val writes: OWrites[ITSAStatusDetail] = (
    (JsPath \ "submittedOn").write[String] and
      (JsPath \ "status").write[Status] and
      (JsPath \ "statusReason").write[StatusReason] and
      (JsPath \ "businessIncomePriorTo2Years").writeNullable[BigDecimal]
  )(w => Tuple.fromProductTyped(w))

}

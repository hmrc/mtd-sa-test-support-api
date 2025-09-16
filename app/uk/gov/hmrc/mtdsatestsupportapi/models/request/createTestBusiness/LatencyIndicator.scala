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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness

import play.api.libs.json.{JsString, Reads, Writes}
import utils.enums.Enums

enum LatencyIndicator {
  case A, Q
}

object LatencyIndicator {
  given Reads[LatencyIndicator] = Enums.reads(values)
  given Writes[LatencyIndicator] = {
    case LatencyIndicator.A => JsString("A")
    case LatencyIndicator.Q => JsString("Q")
  }
  implicit val parser: PartialFunction[String, LatencyIndicator] = Enums.parser(values)
}

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

package uk.gov.hmrc.mtdsatestsupportapi.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait StatusReason

object StatusReason {

  implicit val format: Format[StatusReason] = Enums.format[StatusReason]

  val parser: PartialFunction[String, StatusReason] = Enums.parser[StatusReason]

  case object `Sign up - return available`    extends StatusReason
  case object `Sign up - no return available` extends StatusReason
  case object `ITSA final declaration`        extends StatusReason
  case object `ITSA Q4 declaration`           extends StatusReason
  case object `CESA SA return`                extends StatusReason
  case object `Complex`                       extends StatusReason
  case object `Ceased income source`          extends StatusReason
  case object `Reinstated income source`      extends StatusReason
  case object `Rollover`                      extends StatusReason
  case object `Income Source Latency Changes` extends StatusReason
  case object `MTD ITSA Opt-Out`              extends StatusReason

}

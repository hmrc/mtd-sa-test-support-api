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

package v1.createAmendItsaStatus.model.request

import api.utils.enums.Enums

enum StatusReason(val toDownstream: String) {
  case `Sign up - return available`    extends StatusReason("00")
  case `Sign up - no return available` extends StatusReason("01")
  case `ITSA final declaration`        extends StatusReason("02")
  case `ITSA Q4 declaration`           extends StatusReason("03")
  case `CESA SA return`                extends StatusReason("04")
  case Complex                         extends StatusReason("05")
  case `Ceased income source`          extends StatusReason("06")
  case `Reinstated income source`      extends StatusReason("07")
  case Rollover                        extends StatusReason("08")
  case `Income Source Latency Changes` extends StatusReason("09")
  case `MTD ITSA Opt-Out`              extends StatusReason("10")
  case `MTD ITSA Opt-In`               extends StatusReason("11")
  case `Digitally Exempt`              extends StatusReason("12")
}

object StatusReason {
  val parser: PartialFunction[String, StatusReason] = Enums.parser(values)
}

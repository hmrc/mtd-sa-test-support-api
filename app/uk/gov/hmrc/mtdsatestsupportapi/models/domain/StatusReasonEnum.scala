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

sealed trait StatusReasonEnum {
  val downstreamValue: String
}

object StatusReasonEnum {
  val parser: PartialFunction[String, StatusReasonEnum] = Enums.parser[StatusReasonEnum]
  implicit val format: Format[StatusReasonEnum]         = Enums.format[StatusReasonEnum]

  case object `00` extends StatusReasonEnum {
    val downstreamValue = "Sign up - return available"
  }

  case object `01` extends StatusReasonEnum {
    val downstreamValue = "Sign up - no return available"
  }

  case object `02` extends StatusReasonEnum {
    val downstreamValue = "ITSA final declaration"
  }

  case object `03` extends StatusReasonEnum {
    val downstreamValue = "ITSA Q4 declaration"
  }

  case object `04` extends StatusReasonEnum {
    val downstreamValue = "CESA SA return"
  }

  case object `05` extends StatusReasonEnum {
    val downstreamValue = "Complex"
  }

  case object `06` extends StatusReasonEnum {
    val downstreamValue = "Ceased income source"
  }

  case object `07` extends StatusReasonEnum {
    val downstreamValue = "Reinstated income source"
  }

  case object `08` extends StatusReasonEnum {
    val downstreamValue = "Rollover"
  }

  case object `09` extends StatusReasonEnum {
    val downstreamValue = "Income Source Latency Changes"
  }

//  def toMTD(statusReason: StatusReasonEnum): String = statusReason match {
//    case `Sign up - return available`    => "00"
//    case `Sign up - no return available` => "01"
//    case `ITSA final declaration`        => "02"
//    case `ITSA Q4 declaration`           => "03"
//    case `CESA SA return`                => "04"
//    case `Complex`                       => "05"
//    case `Ceased income source`          => "06"
//    case `Reinstated income source`      => "07"
//    case `Rollover`                      => "08"
//    case `Income Source Latency Changes` => "09"
//
//  }

}

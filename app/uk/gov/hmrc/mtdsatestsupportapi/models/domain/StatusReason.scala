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

import play.api.libs.json.{Reads, Writes}
import utils.enums.Enums

sealed trait StatusReason {
  val downstreamValue: String
}

object StatusReason {

  implicit val writes: Writes[StatusReason] = implicitly[Writes[String]].contramap(_.downstreamValue)

  implicit val reads: Reads[StatusReason] = Enums.reads[StatusReason]

  val parser: PartialFunction[String, StatusReason] = Enums.parser[StatusReason]

  case object `00` extends StatusReason {
    val downstreamValue = "Sign up - return available"
  }

  case object `01` extends StatusReason {
    val downstreamValue = "Sign up - no return available"
  }

  case object `02` extends StatusReason {
    val downstreamValue = "ITSA final declaration"
  }

  case object `03` extends StatusReason {
    val downstreamValue = "ITSA Q4 declaration"
  }

  case object `04` extends StatusReason {
    val downstreamValue = "CESA SA return"
  }

  case object `05` extends StatusReason {
    val downstreamValue = "Complex"
  }

  case object `06` extends StatusReason {
    val downstreamValue = "Ceased income source"
  }

  case object `07` extends StatusReason {
    val downstreamValue = "Reinstated income source"
  }

  case object `08` extends StatusReason {
    val downstreamValue = "Rollover"
  }

  case object `09` extends StatusReason {
    val downstreamValue = "Income Source Latency Changes"
  }

}

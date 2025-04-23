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

package uk.gov.hmrc.mtdsatestsupportapi.models.domain

import play.api.libs.json.{Reads, Writes}
import utils.enums.Enums

sealed trait StatusReason {
  def toDownstream: String
}

object StatusReason {

  implicit val reads: Reads[StatusReason] = Enums.reads[StatusReason]

  implicit val writes: Writes[StatusReason] = Writes.StringWrites.contramap(_.toDownstream)

  val parser: PartialFunction[String, StatusReason] = Enums.parser[StatusReason]

  case object `Sign up - return available` extends StatusReason {
    override def toDownstream = "00"
  }

  case object `Sign up - no return available` extends StatusReason {
    override def toDownstream = "01"
  }

  case object `ITSA final declaration` extends StatusReason {
    override def toDownstream = "02"
  }

  case object `ITSA Q4 declaration` extends StatusReason {
    override def toDownstream = "03"
  }

  case object `CESA SA return` extends StatusReason {
    override def toDownstream = "04"
  }

  case object Complex extends StatusReason {
    override def toDownstream = "05"
  }

  case object `Ceased income source` extends StatusReason {
    override def toDownstream = "06"
  }

  case object `Reinstated income source` extends StatusReason {
    override def toDownstream = "07"
  }

  case object Rollover extends StatusReason {
    override def toDownstream = "08"
  }

  case object `Income Source Latency Changes` extends StatusReason {
    override def toDownstream = "09"
  }

  case object `MTD ITSA Opt-Out` extends StatusReason {
    override def toDownstream = "10"
  }

  case object `MTD ITSA Opt-In` extends StatusReason {
    override def toDownstream = "11"
  }

  case object `Digitally Exempt` extends StatusReason {
    override def toDownstream = "12"
  }
}

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

sealed trait Status {
  def toDownstream: String
}

object Status {

  implicit val reads: Reads[Status] = Enums.reads[Status]

  implicit val writes: Writes[Status] = Writes.StringWrites.contramap(_.toDownstream)

  val parser: PartialFunction[String, Status] = Enums.parser[Status]

  case object `No Status` extends Status {
    override def toDownstream = "00"
  }

  case object `MTD Mandated` extends Status {
    override def toDownstream = "01"
  }

  case object `MTD Voluntary` extends Status {
    override def toDownstream = "02"
  }

  case object Annual extends Status {
    override def toDownstream = "03"
  }

  case object `Non Digital` extends Status {
    override def toDownstream = "04"
  }

  case object Dormant extends Status {
    override def toDownstream = "05"
  }

  case object `MTD Exempt` extends Status {
    override def toDownstream = "99"
  }
}

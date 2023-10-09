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

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}
import utils.enums.Enums

sealed trait Status {
  val downstreamValue: String
}

object Status {

  implicit val writes: Writes[Status] = (obj: Status) => JsString(obj.downstreamValue)

  implicit val reads: Reads[Status] = {
    case JsString("00") => JsSuccess(`00`)
    case JsString("01") => JsSuccess(`01`)
    case JsString("02") => JsSuccess(`02`)
    case JsString("03") => JsSuccess(`03`)
    case JsString("04") => JsSuccess(`04`)
    case JsString("05") => JsSuccess(`05`)
    case JsString("99") => JsSuccess(`99`)
    case _              => JsError("Invalid StatusEnum")
  }

  val parser: PartialFunction[String, Status] = Enums.parser[Status]

  case object `00` extends Status {
    val downstreamValue = "No Status"
  }

  case object `01` extends Status {
    val downstreamValue = "MTD Mandated"
  }

  case object `02` extends Status {
    val downstreamValue = "MTD Voluntary"
  }

  case object `03` extends Status {
    val downstreamValue = "Annual"
  }

  case object `04` extends Status {
    val downstreamValue = "Non Digital"
  }

  case object `05` extends Status {
    val downstreamValue = "Dormant"
  }

  case object `99` extends Status {
    val downstreamValue = "MTD Exempt"
  }

}

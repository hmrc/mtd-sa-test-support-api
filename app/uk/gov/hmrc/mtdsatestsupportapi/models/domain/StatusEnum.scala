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

sealed trait StatusEnum

object StatusEnum {
  implicit val format: Format[StatusEnum] = Enums.format[StatusEnum]

  val parser: PartialFunction[String, StatusEnum] = Enums.parser[StatusEnum]

  case object `00` extends StatusEnum {
    val downstreamValue = "No Status"
  }

  case object `01` extends StatusEnum {
    val downstreamValue = "MTD Mandated"
  }

  case object `02` extends StatusEnum {
    val downstreamValue = "MTD Voluntary"
  }

  case object `03` extends StatusEnum {
    val downstreamValue = "Annual"
  }

  case object `04` extends StatusEnum {
    val downstreamValue = "Non Digital"
  }

  case object `05` extends StatusEnum {
    val downstreamValue = "Dormant"
  }

  case object `99` extends StatusEnum {
    val downstreamValue = "MTD Exempt"
  }

}

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

sealed trait Status

object Status {

  implicit val format: Format[Status] = Enums.format[Status]

  val parser: PartialFunction[String, Status] = Enums.parser[Status]

  case object `No Status`     extends Status
  case object `MTD Mandated`  extends Status
  case object `MTD Voluntary` extends Status
  case object `Annual`        extends Status
  case object `Non Digital`   extends Status
  case object `Dormant`       extends Status
  case object `MTD Exempt`    extends Status
}

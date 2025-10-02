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

enum Status(val toDownstream: String) {
  case `No Status`     extends Status("00")
  case `MTD Mandated`  extends Status("01")
  case `MTD Voluntary` extends Status("02")
  case Annual          extends Status("03")
  case `Non Digital`   extends Status("04")
  case Dormant         extends Status("05")
  case `MTD Exempt`    extends Status("99")
}

object Status {
  implicit val reads: Reads[Status] = Enums.reads(values)
  implicit val writes: Writes[Status] = Writes.StringWrites.contramap(_.toDownstream)
  val parser: PartialFunction[String, Status] = Enums.parser(values)
}

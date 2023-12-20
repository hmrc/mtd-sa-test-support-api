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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness

import play.api.libs.json.{Reads, Writes}
import utils.enums.Enums

sealed trait QuarterlyPeriodType{
  val downstreamValue: String
}

object QuarterlyPeriodType {
   case object `standard` extends QuarterlyPeriodType {
     val downstreamValue = "STANDARD"
   }

   case object `calendar` extends QuarterlyPeriodType {
     val downstreamValue = "CALENDAR"
   }

  implicit val reads: Reads[QuarterlyPeriodType] = Enums.reads[QuarterlyPeriodType]

  implicit val writes: Writes[QuarterlyPeriodType] = implicitly[Writes[String]].contramap(_.downstreamValue)

   val parser: PartialFunction[String, QuarterlyPeriodType] = Enums.parser[QuarterlyPeriodType]
}

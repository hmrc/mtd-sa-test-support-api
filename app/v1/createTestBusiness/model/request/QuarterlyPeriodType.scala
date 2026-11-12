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

package v1.createTestBusiness.model.request

import api.utils.enums.Enums

enum QuarterlyPeriodType(val toDownstream: String) {
  case standard extends QuarterlyPeriodType("STANDARD")
  case calendar extends QuarterlyPeriodType("CALENDAR")
}

object QuarterlyPeriodType {
  val parser: PartialFunction[String, QuarterlyPeriodType] = Enums.parser(values)
}

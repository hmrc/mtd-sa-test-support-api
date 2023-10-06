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

package uk.gov.hmrc.mtdsatestsupportapi.domain

import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusReasonEnum
import utils.enums.EnumJsonSpecSupport

class StatusReasonEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[StatusReasonEnum](
    ("00", StatusReasonEnum.`00`),
    ("01", StatusReasonEnum.`01`),
    ("02", StatusReasonEnum.`02`),
    ("03", StatusReasonEnum.`03`),
    ("04", StatusReasonEnum.`04`),
    ("05", StatusReasonEnum.`05`),
    ("06", StatusReasonEnum.`06`),
    ("07", StatusReasonEnum.`07`),
    ("08", StatusReasonEnum.`08`),
    ("09", StatusReasonEnum.`09`)
  )

}

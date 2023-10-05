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

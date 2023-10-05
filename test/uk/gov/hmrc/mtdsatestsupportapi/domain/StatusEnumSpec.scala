package uk.gov.hmrc.mtdsatestsupportapi.domain

import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.StatusEnum
import utils.enums.EnumJsonSpecSupport

class StatusEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[StatusEnum](
    ("00", StatusEnum.`00`),
    ("01", StatusEnum.`01`),
    ("02", StatusEnum.`02`),
    ("03", StatusEnum.`03`),
    ("04", StatusEnum.`04`),
    ("05", StatusEnum.`05`),
    ("99", StatusEnum.`99`)
  )

}

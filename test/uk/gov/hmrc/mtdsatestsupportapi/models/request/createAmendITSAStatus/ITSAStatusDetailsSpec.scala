package uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus

import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{StatusEnum, StatusReasonEnum}

class ITSAStatusDetailsSpec extends UnitSpec {

  private val itsaStatusDetails = ITSAStatusDetails(
    "2021-03-23T16:02:34.039Z",
    StatusEnum.`00`,
    StatusReasonEnum.`01`,
    None
  )

  "ITSAStatusDetails" when {
    "received API JSON" must {
      "work" in {
        val mtdJson = Json.parse("""
            | {
            |   "submittedOn": "2021-03-23T16:02:34.039Z",
            |   "status": "00",
            |   "statusReason": "01"
            |   }
            |""".stripMargin)

        mtdJson.as[ITSAStatusDetails] shouldBe itsaStatusDetails
      }
    }
  }

}

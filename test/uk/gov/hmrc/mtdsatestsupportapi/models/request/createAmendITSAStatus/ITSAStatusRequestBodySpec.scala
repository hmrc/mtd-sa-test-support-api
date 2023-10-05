package uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus

import api.models.domain.TaxYear
import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{StatusEnum, StatusReasonEnum}

class ITSAStatusRequestBodySpec extends UnitSpec {

  val itsaRequestBody = ITSAStausRequestBody(
    taxYear = TaxYear.fromMtd("2022-23"),
    Seq(ITSAStatusDetails("2021-03-23T16:02:34.039Z", StatusEnum.`00`, StatusReasonEnum.`01`, None))
  )

  "ITSAStatusRequestBody" when {
    "received API JSON" must {
      "work" in {
        val mtdJson = Json.parse(s"""{
            | "taxYear": "2022-23",
            | "itsaStatusDetails": [
            |    {
            |     "submittedOn": "2021-03-23T16:02:34.039Z",
            |     "status": "00",
            |     "statusReason": "01"
            |     }
            |   ]
            |}
            |""".stripMargin)

        mtdJson.as[ITSAStausRequestBody] shouldBe itsaRequestBody
      }
    }
  }

}

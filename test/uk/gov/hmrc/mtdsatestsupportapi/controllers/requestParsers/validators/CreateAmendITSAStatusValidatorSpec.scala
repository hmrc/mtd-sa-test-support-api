package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.models.errors.{NinoFormatError, RuleTaxYearRangeInvalidError, SubmittedOnFormat, TaxYearFormatError}
import api.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRawData

class CreateAmendITSAStatusValidatorSpec extends UnitSpec with JsonErrorValidators {
  val validator       = new CreateAmendITSAStatusValidator()
  private val nino    = "AA123456A"
  private val taxYear = "2023-24"

  private val body = Json.parse(
    """{
      | "itsaStatusDetails": [
      |    {
      |     "submittedOn": "2021-03-23T16:02:34.039Z",
      |     "status": "00",
      |     "statusReason": "01"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val bodyWithBusiness = Json.parse(
    """{
      | "itsaStatusDetails": [
      |    {
      |     "submittedOn": "2021-03-23T16:02:34.039Z",
      |     "status": "00",
      |     "statusReason": "01",
      |     "businessIncomePriorTo2Years": 34999.99
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val bodyWithBadSubmittedOn = Json.parse(
    s"""{
       | "itsaStatusDetails": [
       |    {
       |     "submittedOn": "bad",
       |     "status": "00",
       |     "statusReason": "01",
       |     "businessIncomePriorTo2Years": 34999.99
       |     }
       |   ]
       |}
       |""".stripMargin
  )

  "CreateAmendITSAStatusValidator" must {
    "return no errors" when {
      "a valid ITSA Status without businessIncome2YearsPrior field is provided" in {
        validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body)) shouldBe Nil
      }
      "a valid ITSA Status with businessIncome2YearsPrior field is provided" in {
        validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, bodyWithBusiness)) shouldBe Nil
      }
    }
  }

  "return FORMAT_NINO error" when {
    "Format of the nino provided is not valid" in {
      validator.validate(CreateAmendITSAStatusRawData("hello", taxYear, body)) shouldBe Seq(NinoFormatError)
    }
  }

  "return FORMAT_TAX_YEAR error" when {
    "Format of the tax year provided is not valid" in {
      validator.validate(CreateAmendITSAStatusRawData(nino, "202-23", body)) shouldBe Seq(TaxYearFormatError)
    }
  }

  "return RULE_TAX_YEAR_RANGE_INVALID error" when {
    "Format of the tax year provided is not valid" in {
      validator.validate(CreateAmendITSAStatusRawData(nino, "2021-23", body)) shouldBe Seq(RuleTaxYearRangeInvalidError)
    }
  }

  "return FORMAT_SUBMITTED_ON error" when {
    "format of the submittedOn field is not valid" in {
      validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, bodyWithBadSubmittedOn)) shouldBe
        Seq(SubmittedOnFormat)
    }
  }

//  "return FORMAT_STATUS error" when {
//    "status provided is invalid" in {
//      validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body.update("/itsaStatusDetails/0/status", JsString("294")))) shouldBe Seq(
//        StatusFormat)
//      validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body.removeProperty("itsaStatusDetails/0/status"))) shouldBe Seq(StatusFormat)
//    }
//  }

}

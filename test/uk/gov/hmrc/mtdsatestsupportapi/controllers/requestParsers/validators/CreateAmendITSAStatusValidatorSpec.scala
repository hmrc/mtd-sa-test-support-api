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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.models.errors._
import api.utils.JsonErrorValidators
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRawData

class CreateAmendITSAStatusValidatorSpec extends UnitSpec with JsonErrorValidators {
  private val validator = new CreateAmendITSAStatusValidator()

  private val nino    = "AA123456A"
  private val taxYear = "2023-24"

  private def bodyWith(entries: JsValue*) = Json.parse(s"""
                                                          |{
                                                          | "itsaStatusDetails": ${JsArray(entries)}
                                                          |}
                                                          |""".stripMargin)

  private val itsaStatusDetail = Json.parse("""
                                              |{
                                              |     "submittedOn": "2021-03-23T16:02:34.039Z",
                                              |     "status": "00",
                                              |     "statusReason": "01",
                                              |     "businessIncomePriorTo2Years": 34999.99
                                              |}
                                              |""".stripMargin)

  private val body = bodyWith(itsaStatusDetail)

  "CreateAmendITSAStatusValidator" must {
    "return no errors" when {
      "a valid body is provided" in {
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe Nil
      }

      "a valid body without businessIncome2YearsPrior field is provided" in {
        val body   = bodyWith(itsaStatusDetail.removeProperty("/businessIncomePriorTo2Years"))
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe Nil
      }
    }

    "return FORMAT_NINO error" when {
      "Format of the nino provided is not valid" in {
        val result = validator.validate(CreateAmendITSAStatusRawData("invalid", taxYear, body))

        result shouldBe List(NinoFormatError)
      }
    }

    "return FORMAT_TAX_YEAR error" when {
      "Format of the tax year provided is not valid" in {
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, "202-23", body))

        result shouldBe List(TaxYearFormatError)
      }
    }

    "return RULE_TAX_YEAR_RANGE_INVALID error" when {
      "Format of the tax year provided is not valid" in {
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, "2021-23", body))

        result shouldBe List(RuleTaxYearRangeInvalidError)
      }
    }

    "return FORMAT_SUBMITTED_ON error" when {
      "format of the submittedOn field is not valid" in {
        val body   = bodyWith(itsaStatusDetail.update("/submittedOn", JsString("invalid")))
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(SubmittedOnFormatError.withExtraPath("/itsaStatusDetails/0/submittedOn"))
      }
    }

    "return FORMAT_STATUS error" when {
      "status provided is invalid" in {
        val body   = bodyWith(itsaStatusDetail.update("/status", JsString("294")))
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(StatusFormatError.withExtraPath("/itsaStatusDetails/0/status"))
      }
    }
    "return FORMAT_STATUS_REASON error" when {
      "status reason provided is invalid" in {
        val body   = bodyWith(itsaStatusDetail.update("/statusReason", JsString("54")))
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(StatusReasonFormatError.withExtraPath("/itsaStatusDetails/0/statusReason"))
      }
    }

    "return RULE_INCORRECT_OR_EMPTY_BODY error" when {
      "an empty json object is supplied" in {
        val body   = JsObject.empty
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "mandatory enum fields are missing" in {
        val invalidStatusDetail = itsaStatusDetail.removeProperty("/statusReason").removeProperty("/status")
        val body                = bodyWith(invalidStatusDetail, invalidStatusDetail)
        val result              = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(
          RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/0/status"),
          RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/0/statusReason"),
          RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/1/status"),
          RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/1/statusReason")
        )
      }
      "mandatory non-enum field is missing" in {
        val body   = bodyWith(itsaStatusDetail.removeProperty("/submittedOn"))
        val result = validator.validate(CreateAmendITSAStatusRawData(nino, taxYear, body))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/0/submittedOn"))
      }
    }
  }

}

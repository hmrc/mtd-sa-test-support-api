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

package v1.createAmendItsaStatus

import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.*
import v1.createAmendItsaStatus.model.request.CreateAmendItsaStatusRequestData

class CreateAmendItsaStatusValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2023-24"

  private def validate(nino: String = nino, taxYear: String = taxYear, body: JsValue = mtdRequestBodyJson) = new CreateAmendItsaStatusValidator(
    nino = nino,
    taxYear = taxYear,
    body = body
  ).validateAndWrapResult()

  "CreateAmendItsaStatusValidator" should {
    "return the parsed object" when {
      "a valid request is supplied" in {
        validate() shouldBe Right(CreateAmendItsaStatusRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel))
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validate(nino = "AA1234") shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validate(taxYear = "202324") shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "an invalid tax year range is supplied" in {
        validate(taxYear = "2025-27") shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is supplied" in {
        validate(body = JsObject.empty) shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "an empty itsaStatusDetails array is supplied" in {
        val invalidJson: JsValue = mtdRequestBodyJson.update("/itsaStatusDetails", JsArray.empty)

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/itsaStatusDetails"))
        )
      }

      mtdRequestBodyJson.collectPaths().foreach { path =>
        s"an incorrect type is supplied for field: $path" in {
          val invalidJson = mtdRequestBodyJson.updateArrayOrObjectField(path, JsBoolean(true))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(path)))
        }
      }
    }

    "return SubmittedOnFormatError" when {
      "an invalid submittedOn is supplied" in {
        val invalidJson: JsValue = mtdRequestBodyJson.updateArrayOrObjectField("/itsaStatusDetails/0/submittedOn", JsString("2024-03-23"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, SubmittedOnFormatError.withPath("/itsaStatusDetails/0/submittedOn"))
        )
      }
    }

    "return StatusFormatError" when {
      "an invalid status is supplied" in {
        val invalidJson: JsValue = mtdRequestBodyJson.updateArrayOrObjectField("/itsaStatusDetails/0/status", JsString("invalid status"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, StatusFormatError.withPath("/itsaStatusDetails/0/status"))
        )
      }
    }

    "return StatusReasonFormatError" when {
      "an invalid statusReason is supplied" in {
        val invalidJson: JsValue = mtdRequestBodyJson.updateArrayOrObjectField("/itsaStatusDetails/0/statusReason", JsString("invalid reason"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, StatusReasonFormatError.withPath("/itsaStatusDetails/0/statusReason"))
        )
      }
    }

    "return BusinessIncome2YearsPriorFormatError" when {
      "an invalid businessIncome2YearsPrior is supplied" in {
        val invalidJson: JsValue = mtdRequestBodyJson.updateArrayOrObjectField("/itsaStatusDetails/0/businessIncome2YearsPrior", JsNumber(-1))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, BusinessIncome2YearsPriorFormatError.withPath("/itsaStatusDetails/0/businessIncome2YearsPrior"))
        )
      }
    }

    "return DuplicateSubmittedOnError" when {
      "duplicate submittedOn values are supplied" in {
        val invalidJson: JsValue = Json.obj("itsaStatusDetails" -> Seq.fill(2)(mtdItsaStatusDetailJson))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, DuplicateSubmittedOnError)
        )
      }
    }
  }

}

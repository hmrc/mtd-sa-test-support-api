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

package v1.createTestBusiness

import api.models.domain.Nino
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.*
import v1.createTestBusiness.model.request.*
import v1.createTestBusiness.model.request.TypeOfBusiness.*

import java.time.*

class CreateTestBusinessValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val nino: String = "AA123456A"

  private def validate(nino: String = nino, body: JsValue = mtdSelfEmploymentRequestBodyJson) = new CreateTestBusinessValidator(
    nino = nino,
    body = body
  ).validateAndWrapResult()

  "CreateTestBusinessValidator" should {
    "return the parsed object" when {
      Seq(
        (mtdSelfEmploymentRequestBodyJson, maxSelfEmploymentRequestBodyModel),
        (mtdPropertyRequestBodyJson(`uk-property`), maxPropertyRequestBodyModel(`uk-property`)),
        (mtdPropertyRequestBodyJson(`foreign-property`), maxPropertyRequestBodyModel(`foreign-property`)),
        (mtdPropertyRequestBodyJson(`property-unspecified`), maxPropertyRequestBodyModel(`property-unspecified`))
      ).foreach { (body, model) =>
        s"a valid request is supplied for typeOfBusiness: ${model.typeOfBusiness}" in {
          validate(body = body) shouldBe Right(CreateTestBusinessRequestData(Nino(nino), model))
        }
      }

      "a minimum valid request is supplied" in {
        val minimumRequestBody: JsValue = Json.obj("typeOfBusiness" -> "uk-property")

        validate(body = minimumRequestBody) shouldBe Right(
          CreateTestBusinessRequestData(Nino(nino), minPropertyRequestBodyModel(`uk-property`))
        )
      }

      "no postcode is supplied when country code is not GB" in {
        val bodyWithoutPostcode: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty("/businessAddressPostcode")

        validate(
          body = bodyWithoutPostcode
        ) shouldBe Right(CreateTestBusinessRequestData(Nino(nino), maxSelfEmploymentRequestBodyModel.copy(businessAddressPostcode = None)))
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validate(nino = "AA1234") shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is supplied" in {
        validate(body = JsObject.empty) shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      mtdSelfEmploymentRequestBodyJson.collectPaths().foreach { path =>
        s"an incorrect type is supplied for field: $path" in {
          val invalidJson = mtdSelfEmploymentRequestBodyJson.update(path, JsBoolean(true))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(path)))
        }
      }
    }

    "return TypeOfBusinessFormatError" when {
      "an invalid typeOfBusiness is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/typeOfBusiness", JsString("property"))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, TypeOfBusinessFormatError.withPath("/typeOfBusiness")))
      }
    }

    "return StringFormatError" when {
      Seq(
        ("/tradingType", "a" * 36),
        ("/tradingName", "a" * 106),
        ("/businessAddressLineOne", "a" * 36),
        ("/businessAddressLineTwo", "a" * 36),
        ("/businessAddressLineThree", "a" * 36),
        ("/businessAddressLineFour", "a" * 36)
      ).foreach { (path, value) =>
        s"an invalid string is supplied for field: $path" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update(path, JsString(value))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, StringFormatError.withPath(path)))
        }
      }
    }

    "return RuleUnexpectedTradingTypeError" when {
      "tradingType is provided for a property business" in {
        val invalidJson: JsValue = mtdPropertyRequestBodyJson(`uk-property`).update("/tradingType", JsString("Plastering"))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleUnexpectedTradingTypeError))
      }
    }

    "return RuleMissingTradingTypeError" when {
      "tradingType is missing for a self-employment business" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty("/tradingType")

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleMissingTradingTypeError))
      }
    }

    "return RuleUnexpectedTradingNameError" when {
      "tradingName is provided for a property business" in {
        val invalidJson: JsValue = mtdPropertyRequestBodyJson(`uk-property`).update("/tradingName", JsString("ABC Ltd"))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleUnexpectedTradingNameError))
      }
    }

    "return RuleMissingTradingNameError" when {
      "tradingName is missing for a self-employment business" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty("/tradingName")

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleMissingTradingNameError))
      }
    }

    "return DateFormatError" when {
      Seq(
        "/firstAccountingPeriodStartDate",
        "/firstAccountingPeriodEndDate",
        "/latencyDetails/latencyEndDate",
        "/commencementDate",
        "/cessationDate"
      ).foreach { path =>
        s"an invalid date is supplied for field: $path" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update(path, JsString("2024"))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, DateFormatError.withPath(path)))
        }
      }
    }

    "return RuleFirstAccountingDateRangeInvalidError" when {
      "the first accounting period date range is not a full tax year" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/firstAccountingPeriodStartDate", JsString("2023-01-01"))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleFirstAccountingDateRangeInvalidError))
      }
    }

    "return MissingFirstAccountingPeriodStartDateError" when {
      "the firstAccountingPeriodStartDate is missing" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty("/firstAccountingPeriodStartDate")

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, MissingFirstAccountingPeriodStartDateError))
      }
    }

    "return MissingFirstAccountingPeriodEndDateError" when {
      "the firstAccountingPeriodEndDate is missing" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty("/firstAccountingPeriodEndDate")

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, MissingFirstAccountingPeriodEndDateError))
      }
    }

    "return TaxYearFormatError" when {
      Seq(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2",
        "/quarterlyTypeChoice/taxYearOfChoice"
      ).foreach { path =>
        s"an invalid tax year is supplied for field: $path" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update(path, JsString("2024"))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError.withPath(path)))
        }
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      Seq(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2",
        "/quarterlyTypeChoice/taxYearOfChoice"
      ).foreach { path =>
        s"an invalid tax year range is supplied for field: $path" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update(path, JsString("2025-27"))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError.withPath(path)))
        }
      }
    }

    "return LatencyIndicatorFormatError" when {
      Seq(
        "/latencyDetails/latencyIndicator1",
        "/latencyDetails/latencyIndicator2"
      ).foreach { path =>
        s"an invalid latency indicator is supplied for field: $path" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update(path, JsString("B"))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, LatencyIndicatorFormatError.withPath(path)))
        }
      }
    }

    "return QuarterlyPeriodTypeFormatError" when {
      "an invalid quarterlyPeriodType is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/quarterlyTypeChoice/quarterlyPeriodType", JsString("quarter"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, QuarterlyPeriodTypeFormatError.withPath("/quarterlyTypeChoice/quarterlyPeriodType"))
        )
      }
    }

    "return AccountingTypeFormatError" when {
      "an invalid accountingType is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/accountingType", JsString("COIN"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, AccountingTypeFormatError.withPath("/accountingType"))
        )
      }
    }

    "return RuleCommencementDateNotSupportedError" when {
      "the commencement date is not in the past" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson
          .update("/commencementDate", JsString(LocalDate.now(ZoneOffset.UTC).toString))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleCommencementDateNotSupportedError))
      }
    }

    "return CountryCodeFormatError" when {
      "an invalid businessAddressCountryCode is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/businessAddressCountryCode", JsString("FRANCE"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/businessAddressCountryCode"))
        )
      }

      "an invalid ISO 3166-1 alpha-2 businessAddressCountryCode is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/businessAddressCountryCode", JsString("FF"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, CountryCodeFormatError.forRuleCountryCode.withPath("/businessAddressCountryCode"))
        )
      }
    }

    "return PostcodeFormatError" when {
      "an invalid businessAddressPostcode is supplied" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.update("/businessAddressPostcode", JsString("AB1"))

        validate(body = invalidJson) shouldBe Left(
          ErrorWrapper(correlationId, PostcodeFormatError.withPath("/businessAddressPostcode"))
        )
      }
    }

    "return RuleUnexpectedBusinessAddressError" when {
      Seq(
        ("/businessAddressLineOne", "Line 1 of address"),
        ("/businessAddressLineTwo", "Line 2 of address"),
        ("/businessAddressLineThree", "Line 3 of address"),
        ("/businessAddressLineFour", "Line 4 of address"),
        ("/businessAddressPostcode", "AB1 2CD"),
        ("/businessAddressCountryCode", "FR")
      ).foreach { (path, value) =>
        s"the unexpected field `$path` is supplied for a property business" in {
          val invalidJson: JsValue = mtdPropertyRequestBodyJson(`uk-property`).update(path, JsString(value))

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleUnexpectedBusinessAddressError))
        }
      }
    }

    "return RuleMissingBusinessAddressError" when {
      Seq("/businessAddressLineOne", "/businessAddressCountryCode").foreach { path =>
        s"the field `$path` is not supplied for a self-employment business" in {
          val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson.removeProperty(path)

          validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, RuleMissingBusinessAddressError))
        }
      }
    }

    "return MissingPostcodeError" when {
      "the businessAddressPostcode is not supplied when country code is GB" in {
        val invalidJson: JsValue = mtdSelfEmploymentRequestBodyJson
          .removeProperty("/businessAddressPostcode")
          .update("/businessAddressCountryCode", JsString("GB"))

        validate(body = invalidJson) shouldBe Left(ErrorWrapper(correlationId, MissingPostcodeError))
      }
    }
  }

}

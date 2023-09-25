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
import config.FeatureSwitches
import org.scalactic.source.Position
import play.api.libs.json.{JsString, Json}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateTestBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.CreateTestBusinessRawData

import java.time._

class CreateTestBusinessValidatorSpec extends UnitSpec with JsonErrorValidators with CreateTestBusinessFixtures {

  private val validNino = "AA123456A"

  private val now                         = Instant.parse("2020-01-01T10:11:12.123Z")
  private val clock                       = Clock.fixed(now, ZoneOffset.UTC)
  private def localDate(instant: Instant) = LocalDate.ofInstant(instant, ZoneOffset.UTC)

  private val timeInPast = localDate(now.minus(Duration.ofDays(1)))

  private val bodySelfEmploymentValid = Json.parse(
    s"""{
       |  "typeOfBusiness": "self-employment",
       |  "tradingName": "Abc Ltd",
       |  "firstAccountingPeriodStartDate": "2022-04-06",
       |  "firstAccountingPeriodEndDate": "2023-04-05",
       |  "latencyDetails": {
       |    "latencyEndDate": "2020-01-01",
       |    "taxYear1": "2020-21",
       |    "latencyIndicator1": "A",
       |    "taxYear2": "2021-22",
       |    "latencyIndicator2": "Q"
       |  },
       |  "accountingType": "CASH",
       |  "commencementDate": ${timeInPast.toJson},
       |  "cessationDate": "2030-01-01",
       |  "businessAddressLineOne": "Address line 1",
       |  "businessAddressLineTwo": "Address line 2",
       |  "businessAddressLineThree": "Address line 3",
       |  "businessAddressLineFour": "Address line 4",
       |  "businessAddressPostcode": "SW1A 1AA",
       |  "businessAddressCountryCode": "GB"
       |}
       |""".stripMargin
  )

  private val bodyUkPropertyValid = Json.parse(
    s"""{
       |  "typeOfBusiness": "uk-property",
       |  "firstAccountingPeriodStartDate": "2022-04-06",
       |  "firstAccountingPeriodEndDate": "2023-04-05",
       |  "latencyDetails": {
       |    "latencyEndDate": "2020-01-01",
       |    "taxYear1": "2020-21",
       |    "latencyIndicator1": "A",
       |    "taxYear2": "2021-22",
       |    "latencyIndicator2": "Q"
       |  },
       |  "accountingType": "CASH",
       |  "commencementDate": ${timeInPast.toJson},
       |  "cessationDate": "2030-01-01"
       |}
       |""".stripMargin
  )

  class Test {
    private val featureSwitches = mock[FeatureSwitches]
    (featureSwitches.isEnabled(_: String)).stubs("release5").returns(true)

    val validator = new CreateTestBusinessValidator(clock, featureSwitches)
  }

  "CreateTestBusinessValidator" must {
    "return no errors" when {
      "a valid business is supplied of type Self Employment" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid)) shouldBe Nil
      }

      "a valid business is supplied of type UK Property" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodyUkPropertyValid)) shouldBe Nil
      }

      "a valid minimal business is supplied" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson)) shouldBe Nil
      }

      "no postcode is supplied when country code is not GB" in new Test {
        validator.validate(
          CreateTestBusinessRawData(
            validNino,
            bodySelfEmploymentValid
              .removeProperty("/businessAddressPostcode")
              .update("/businessAddressCountryCode", JsString("FR"))
          )
        ) shouldBe Nil
      }
    }

    "return FORMAT_NINO error" when {
      "format of the nino is not valid" in new Test {
        validator.validate(CreateTestBusinessRawData("BAD NINO", bodySelfEmploymentValid)) shouldBe Seq(NinoFormatError)
      }
    }

    "return FORMAT_TYPE_OF_BUSINESS error" when {
      "format of the business type field is not valid" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/typeOfBusiness", JsString("badValue")))) shouldBe Seq(
          TypeOfBusinessFormatError)
      }
    }

    "return FORMAT_TAX_YEAR error with the appropriate path" when {
      def testWith(path: String)(implicit pos: Position): Unit = {
        s"when the format of a tax year is not valid for $path" in new Test {
          validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update(path, JsString("badValue")))) shouldBe
            Seq(TaxYearFormatError.withExtraPath(path))
        }
      }

      testWith("/latencyDetails/taxYear1")
      testWith("/latencyDetails/taxYear2")
    }

    "return RULE_TAX_YEAR_RANGE_INVALID error with the appropriate path" when {
      def testWith(path: String)(implicit pos: Position): Unit = {
        s"when a tax year has an invalid range for $path" in new Test {
          validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update(path, JsString("2020-22")))) shouldBe
            Seq(RuleTaxYearRangeInvalidError.withExtraPath(path))
        }
      }

      testWith("/latencyDetails/taxYear1")
      testWith("/latencyDetails/taxYear2")
    }

    "return FORMAT_DATE error with the appropriate path" when {
      def testWith(path: String)(implicit pos: Position): Unit = {
        s"when the format of a date is not valid for $path" in new Test {
          validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update(path, JsString("badValue")))) shouldBe
            Seq(DateFormatError.withExtraPath(path))
        }
      }

      testWith("/firstAccountingPeriodStartDate")
      testWith("/firstAccountingPeriodEndDate")
      testWith("/latencyDetails/latencyEndDate")
      testWith("/commencementDate")
      testWith("/cessationDate")
    }

    "return RULE_FIRST_ACCOUNTING_DATE_RANGE_INVALID when the first accounting period date range is not a full tax year" in new Test {
      validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/firstAccountingPeriodStartDate", JsString("2023-01-01")))) shouldBe
        Seq(RuleFirstAccountingDateRangeInvalid)
    }

    "return MISSING_FIRST_ACCOUNTING_PERIOD_START_DATE when the the first accounting period start date is missing" in new Test {
      validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.removeProperty("/firstAccountingPeriodStartDate"))) shouldBe
        Seq(MissingFirstAccountingPeriodStartDateError)
    }

    "return MISSING_FIRST_ACCOUNTING_PERIOD_END_DATE when the the first accounting period end date is missing" in new Test {
      validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.removeProperty("/firstAccountingPeriodEndDate"))) shouldBe
        Seq(MissingFirstAccountingPeriodEndDateError)
    }

    "return no errors when both the the first accounting period start and end dates are missing" in new Test {
      validator.validate(
        CreateTestBusinessRawData(
          validNino,
          bodySelfEmploymentValid
            .removeProperty("/firstAccountingPeriodStartDate")
            .removeProperty("/firstAccountingPeriodEndDate")
        )
      ) shouldBe Nil
    }

    "return FORMAT_ACCOUNTING_TYPE error" when {
      "format of the accounting type field is not valid" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/accountingType", JsString("badValue")))) shouldBe
          Seq(AccountingTypeFormatError)
      }
    }

    "return RULE_COMMENCEMENT_DATE_NOT_SUPPORTED error" when {
      "the commencement date is not in the past" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/commencementDate", Json.toJson(localDate(now))))) shouldBe
          Seq(RuleCommencementDateNotSupported)
      }
    }

    "return FORMAT_POSTCODE error" when {
      "the format of the postcode field is not valid" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/businessAddressPostcode", JsString("badValue")))) shouldBe
          Seq(PostcodeFormatError)
      }
    }

    "return FORMAT_LATENCY_INDICATOR error with the appropriate path" when {
      def testWith(path: String)(implicit pos: Position): Unit = {
        s"when the format of a latency indicator is not valid for $path" in new Test {
          validator.validate(CreateTestBusinessRawData(validNino, bodyUkPropertyValid.update(path, JsString("badValue")))) shouldBe
            Seq(LatencyIndicatorFormatError.withExtraPath(path))
        }
      }

      testWith("/latencyDetails/latencyIndicator1")
      testWith("/latencyDetails/latencyIndicator2")
    }

    "return MISSING_POSTCODE error" when {
      "no post code is supplied when country code is GB" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.removeProperty("/businessAddressPostcode"))) shouldBe
          Seq(MissingPostcodeError)
      }
    }

    "return FORMAT_COUNTRY_CODE error" when {
      "a the country code is not valid" in new Test {
        validator.validate(CreateTestBusinessRawData(validNino, bodySelfEmploymentValid.update("/businessAddressCountryCode", JsString("badValue")))) shouldBe
          Seq(CountryCodeFormatError)
      }
    }

    "return RULE_UNEXPECTED_BUSINESS_ADDRESS error" when {
      def testWithForbiddenAddressField(path: String, value: String)(implicit pos: Position): Unit = {
        s"the forbidden $path is provided" in new Test {
          validator.validate(
            CreateTestBusinessRawData(
              nino = validNino,
              bodyUkPropertyValid.update(path, JsString(value))
            )
          ) shouldBe Seq(RuleUnexpectedBusinessAddress)
        }
      }

      testWithForbiddenAddressField(path = "/businessAddressLineOne",     value = "Addr Line 1")
      testWithForbiddenAddressField(path = "/businessAddressLineTwo",     value = "Addr Line 2")
      testWithForbiddenAddressField(path = "/businessAddressLineThree",   value = "Addr Line 3")
      testWithForbiddenAddressField(path = "/businessAddressLineFour",    value = "Addr Line 4")
      testWithForbiddenAddressField(path = "/businessAddressPostcode",    value = "SW1A 1AA")
      testWithForbiddenAddressField(path = "/businessAddressCountryCode", value = "FR")
    }

    "return RULE_MISSING_BUSINESS_ADDRESS error" when {
      def testWithMissingAddressField(path: String)(implicit pos: Position): Unit = {
        s"the mandatory $path is missing" in new Test {
          validator.validate(
            CreateTestBusinessRawData(
              nino = validNino,
              bodySelfEmploymentValid.removeProperty(path)
            )
          ) shouldBe Seq(RuleMissingBusinessAddress)
        }
      }

      testWithMissingAddressField("/businessAddressLineOne")
      testWithMissingAddressField("/businessAddressCountryCode")
    }

    "return RULE_UNEXPECTED_TRADING_NAME error" when {
      "tradingName is provided" in new Test {
        validator.validate(
          CreateTestBusinessRawData(
            nino = validNino,
            bodyUkPropertyValid.update("/tradingName", JsString("Trading Name"))
          )
        ) shouldBe Seq(RuleUnexpectedTradingName)
      }
    }

    "return RULE_MISSING_TRADING_NAME error" when {
      "tradingName is missing" in new Test {
        validator.validate(
          CreateTestBusinessRawData(
            nino = validNino,
            bodySelfEmploymentValid.removeProperty("/tradingName")
          )
        ) shouldBe Seq(RuleMissingTradingName)
      }
    }

    "return RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED" when {
      def testWith(path: String)(implicit pos: Position): Unit = {
        s"a mandatory $path field is missed" in new Test {
          validator.validate(
            CreateTestBusinessRawData(
              nino = validNino,
              bodySelfEmploymentValid.removeProperty(path)
            )
          ) shouldBe List(RuleIncorrectOrEmptyBodyError.withExtraPath(path))
        }
      }

      testWith("/typeOfBusiness")
      testWith("/latencyDetails/latencyEndDate")
      testWith("/latencyDetails/taxYear1")
      testWith("/latencyDetails/latencyIndicator1")
      testWith("/latencyDetails/taxYear2")
      testWith("/latencyDetails/latencyIndicator2")
    }
  }

}

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

  private val body = Json.parse(s"""{
      |  "typeOfBusiness": "self-employment",
      |  "tradingName": "Abc Ltd",
      |  "firstAccountingPeriodStartDate": "2002-02-02",
      |  "firstAccountingPeriodEndDate": "2012-12-12",
      |  "latencyDetails": {
      |    "latencyEndDate": "2020-01-01",
      |    "taxYear1": "2020-21",
      |    "latencyIndicator1": "A",
      |    "taxYear2": "2021-22",
      |    "latencyIndicator2": "Q"
      |  },
      |  "accountingType": "CASH",
      |  "commencementDate": "$timeInPast",
      |  "cessationDate": "2030-01-01",
      |  "businessAddressLineOne": "L1",
      |  "businessAddressLineTwo": "L2",
      |  "businessAddressLineThree": "L3",
      |  "businessAddressLineFour": "L4",
      |  "businessAddressPostcode": "SW1A 1AA",
      |  "businessAddressCountryCode": "GB"
      |}
      |""".stripMargin)

  val validator = new CreateTestBusinessValidator(clock)

  "CreateTestBusinessValidator" must {
    "return no errors" when {
      "a valid business is supplied" in {
        validator.validate(CreateTestBusinessRawData(validNino, body)) shouldBe Nil
      }

      "a valid minimal business is supplied" in {
        validator.validate(CreateTestBusinessRawData(validNino, MinimalCreateTestBusinessRequest.mtdBusinessJson)) shouldBe Nil
      }
    }

    "return FORMAT_NINO error" when {
      "format of the nino is not valid" in {
        validator.validate(CreateTestBusinessRawData("BAD NINO", body)) shouldBe Seq(NinoFormatError)
      }
    }

    "return FORMAT_TYPE_OF_BUSINESS error" when {
      "format of the business type field is not valid" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.update("/typeOfBusiness", JsString("badValue")))) shouldBe Seq(
          TypeOfBusinessFormatError)
      }
    }

    "return FORMAT_TAX_YEAR error with the appropriate path" when {
      Seq(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2"
      ).foreach(testWith)

      def testWith(path: String): Unit =
        s"when the format of a tax year is not valid for $path" in {
          validator.validate(CreateTestBusinessRawData(validNino, body.update(path, JsString("badValue")))) shouldBe
            Seq(TaxYearFormatError.withExtraPath(path))
        }
    }

    "return RULE_TAX_YEAR_RANGE_INVALID error with the appropriate path" when {
      Seq(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2"
      ).foreach(testWith)

      def testWith(path: String): Unit =
        s"when a tax year has an invalid range for $path" in {
          validator.validate(CreateTestBusinessRawData(validNino, body.update(path, JsString("2020-22")))) shouldBe
            Seq(RuleTaxYearRangeInvalidError.withExtraPath(path))
        }
    }

    "return FORMAT_DATE error with the appropriate path" when {

      Seq(
        "/firstAccountingPeriodStartDate",
        "/firstAccountingPeriodEndDate",
        "/latencyDetails/latencyEndDate",
        "/commencementDate",
        "/cessationDate"
      ).foreach(testWith)

      def testWith(path: String): Unit =
        s"when the format of a date is not valid for $path" in {
          validator.validate(CreateTestBusinessRawData(validNino, body.update(path, JsString("badValue")))) shouldBe
            Seq(DateFormatError.withExtraPath(path))
        }
    }

    "return FORMAT_ACCOUNTING_TYPE error" when {
      "format of the accounting type field is not valid" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.update("/accountingType", JsString("badValue")))) shouldBe
          Seq(AccountingTypeFormatError)
      }
    }

    "return RULE_COMMENCEMENT_DATE_NOT_SUPPORTED error" when {
      "the commencement date is not in the past" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.update("/commencementDate", Json.toJson(localDate(now))))) shouldBe
          Seq(RuleCommencementDateNotSupported)
      }
    }

    "return FORMAT_POSTCODE error" when {
      "the format of the postcode field is not valid" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.update("/businessAddressPostcode", JsString("badValue")))) shouldBe
          Seq(PostcodeFormatError)
      }
    }

    "return FORMAT_LATENCY_INDICATOR error with the appropriate path" when {
      Seq(
        "/latencyDetails/latencyIndicator1",
        "/latencyDetails/latencyIndicator2"
      ).foreach(testWith)

      def testWith(path: String): Unit =
        s"when the format of a latency indicator is not valid for $path" in {
          validator.validate(CreateTestBusinessRawData(validNino, body.update(path, JsString("badValue")))) shouldBe
            Seq(LatencyIndicatorFormatError.withExtraPath(path))
        }
    }

    "return MISSING_POSTCODE error" when {
      "no post code is supplied when country code is GB" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.removeProperty("/businessAddressPostcode"))) shouldBe
          Seq(MissingPostcodeError)
      }
    }

    "return no errors" when {
      "no post code is supplied when country code is not GB" in {
        validator.validate(
          CreateTestBusinessRawData(
            validNino,
            body
              .removeProperty("/businessAddressPostcode")
              .update("/businessAddressCountryCode", JsString("FR")))) shouldBe Nil
      }
    }

    "return FORMAT_COUNTRY_CODE error" when {
      "a the country code is not valid" in {
        validator.validate(CreateTestBusinessRawData(validNino, body.update("/businessAddressCountryCode", JsString("badValue")))) shouldBe
          Seq(CountryCodeFormatError)
      }
    }

    "return RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED" when {
      Seq(
        "/typeOfBusiness",
        "/latencyDetails/latencyEndDate",
        "/latencyDetails/taxYear1",
        "/latencyDetails/latencyIndicator1",
        "/latencyDetails/taxYear2",
        "/latencyDetails/latencyIndicator2"
      ).foreach(testWith)

      def testWith(path: String): Unit =
        s"a mandatory $path field is missed" in {
          validator.validate(
            CreateTestBusinessRawData(
              nino = validNino,
              body.removeProperty(path)
            )) shouldBe List(RuleIncorrectOrEmptyBodyError.withExtraPath(path))
        }
    }
  }

}

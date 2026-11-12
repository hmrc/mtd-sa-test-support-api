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

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v1.createTestBusiness.fixture.CreateTestBusinessFixture.*

import java.time.LocalDate

class CreateTestBusinessControllerISpec extends IntegrationBaseSpec {

  private val allInvalidFormatsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "typeOfBusiness": "employment",
      |  "tradingType": "",
      |  "tradingName": "",
      |  "firstAccountingPeriodStartDate": "2020",
      |  "firstAccountingPeriodEndDate": "2021",
      |  "latencyDetails": {
      |    "latencyEndDate": "2022",
      |    "taxYear1": "2020",
      |    "latencyIndicator1": "B",
      |    "taxYear2": "2021",
      |    "latencyIndicator2": "C"
      |  },
      |  "quarterlyTypeChoice": {
      |    "quarterlyPeriodType": "period",
      |    "taxYearOfChoice": "2023"
      |  },
      |  "accountingType": "MONEY",
      |  "commencementDate": "2020",
      |  "cessationDate": "2025",
      |  "businessAddressLineOne": "",
      |  "businessAddressLineTwo": "",
      |  "businessAddressLineThree": "",
      |  "businessAddressLineFour": "",
      |  "businessAddressPostcode": "NW2",
      |  "businessAddressCountryCode": "G"
      |}
    """.stripMargin
  )

  private val selfEmploymentRuleErrorsRequestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "typeOfBusiness": "self-employment",
      |  "firstAccountingPeriodStartDate": "2023-04-06",
      |  "firstAccountingPeriodEndDate": "2024-04-06",
      |  "latencyDetails": {
      |    "latencyEndDate": "2022-04-06",
      |    "taxYear1": "2023-25",
      |    "latencyIndicator1": "A",
      |    "taxYear2": "2024-26",
      |    "latencyIndicator2": "Q"
      |  },
      |  "quarterlyTypeChoice": {
      |    "quarterlyPeriodType": "standard",
      |    "taxYearOfChoice": "2023-25"
      |  },
      |  "commencementDate": "${LocalDate.now.plusYears(1)}",
      |  "businessAddressCountryCode": "GB"
      |}
    """.stripMargin
  )

  private val propertyRuleErrorsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "typeOfBusiness": "uk-property",
      |  "tradingType": "Plastering",
      |  "tradingName": "ABC Ltd",
      |  "firstAccountingPeriodStartDate": "2023-04-06",
      |  "businessAddressLineOne": "221b Baker Street"
      |}
    """.stripMargin
  )

  private val allInvalidFormatsRequestErrors: List[MtdError] = List(
    AccountingTypeFormatError.withPath("/accountingType"),
    CountryCodeFormatError.withPath("/businessAddressCountryCode"),
    DateFormatError.withPaths(
      List(
        "/firstAccountingPeriodStartDate",
        "/firstAccountingPeriodEndDate",
        "/latencyDetails/latencyEndDate",
        "/commencementDate",
        "/cessationDate"
      )
    ),
    LatencyIndicatorFormatError.withPaths(
      List(
        "/latencyDetails/latencyIndicator1",
        "/latencyDetails/latencyIndicator2"
      )
    ),
    PostcodeFormatError.withPath("/businessAddressPostcode"),
    QuarterlyPeriodTypeFormatError.withPath("/quarterlyTypeChoice/quarterlyPeriodType"),
    StringFormatError.withPaths(
      List(
        "/tradingType",
        "/tradingName",
        "/businessAddressLineOne",
        "/businessAddressLineTwo",
        "/businessAddressLineThree",
        "/businessAddressLineFour"
      )
    ),
    TaxYearFormatError.withPaths(
      List(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2",
        "/quarterlyTypeChoice/taxYearOfChoice"
      )
    ),
    TypeOfBusinessFormatError.withPath("/typeOfBusiness")
  )

  private val selfEmploymentRuleRequestErrors: List[MtdError] = List(
    MissingPostcodeError,
    RuleCommencementDateNotSupportedError,
    RuleFirstAccountingDateRangeInvalidError,
    RuleMissingBusinessAddressError,
    RuleMissingTradingNameError,
    RuleMissingTradingTypeError,
    RuleTaxYearRangeInvalidError.withPaths(
      List(
        "/latencyDetails/taxYear1",
        "/latencyDetails/taxYear2",
        "/quarterlyTypeChoice/taxYearOfChoice"
      )
    )
  )

  private val propertyRuleRequestErrors: List[MtdError] = List(
    MissingFirstAccountingPeriodEndDateError,
    RuleUnexpectedBusinessAddressError,
    RuleUnexpectedTradingNameError,
    RuleUnexpectedTradingTypeError
  )

  private def wrappedErrors(errors: Seq[MtdError]): ErrorWrapper = ErrorWrapper(
    correlationId = "ignored",
    error = BadRequestError,
    errors = Some(errors)
  )

  "Calling the 'Create a Test Business' endpoint" should {
    "return a 201 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = POST,
          uri = downstreamUri,
          status = CREATED,
          body = downstreamResponseBodyJson
        )

        val response: WSResponse = await(request().post(mtdSelfEmploymentRequestBodyJson))

        response.status shouldBe CREATED
        response.json shouldBe mtdResponseBodyJson
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedError: MtdError,
                              expectedErrors: Option[ErrorWrapper],
                              scenario: Option[String]): Unit = {
        s"validation fails with ${expectedError.code} error${scenario.fold("")(scenario => s" for '$scenario' scenario")}" in new Test {
          override val nino: String = requestNino

          val response: WSResponse = await(request().post(requestBody))

          response.status shouldBe expectedStatus
          response.json shouldBe expectedErrors.fold(expectedError.asJson)(Json.toJson(_))
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val validationInput: Seq[(String, JsValue, Int, MtdError, Option[ErrorWrapper], Option[String])] = List(
        ("AA1123A", mtdSelfEmploymentRequestBodyJson, BAD_REQUEST, NinoFormatError, None, None),
        ("AA123456A", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, None),
        (
          "AA123456A",
          allInvalidFormatsRequestBodyJson,
          BAD_REQUEST,
          BadRequestError,
          Some(wrappedErrors(allInvalidFormatsRequestErrors)),
          Some("all invalid formats")
        ),
        (
          "AA123456A",
          selfEmploymentRuleErrorsRequestBodyJson,
          BAD_REQUEST,
          BadRequestError,
          Some(wrappedErrors(selfEmploymentRuleRequestErrors)),
          Some("self employment rule errors")
        ),
        (
          "AA123456A",
          propertyRuleErrorsRequestBodyJson,
          BAD_REQUEST,
          BadRequestError,
          Some(wrappedErrors(propertyRuleRequestErrors)),
          Some("property rule errors")
        )
      )

      validationInput.foreach(validationErrorTest.tupled)

      "stub returns a code DUPLICATE_PROPERTY_BUSINESS error and status 400" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onError(
          method = POST,
          uri = downstreamUri,
          errorStatus = BAD_REQUEST,
          errorBody = downstreamErrorBody("DUPLICATE_PROPERTY_BUSINESS")
        )

        val response: WSResponse = await(request().post(mtdSelfEmploymentRequestBodyJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe RulePropertyBusinessAddedError.asJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }

  private trait Test {
    val nino: String = "AA123456A"

    val downstreamUri: String = s"/test-support/business-details/$nino"

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest(s"/business/$nino")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7")
        )
    }

  }

}

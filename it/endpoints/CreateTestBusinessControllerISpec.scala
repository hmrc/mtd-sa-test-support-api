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

package endpoints

import api.models.errors.{AccountingTypeFormatError, CountryCodeFormatError, DateFormatError, LatencyIndicatorFormatError, MissingPostcodeError, MtdError, NinoFormatError, PostcodeFormatError, RuleCommencementDateNotSupported, RuleIncorrectOrEmptyBodyError, RulePropertyBusinessAddedError, RuleTaxYearRangeInvalidError, TaxYearFormatError, TypeOfBusinessFormatError}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.{IntegrationBaseSpec, UnitSpec}
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateTestBusinessFixtures

import java.time.LocalDate

class CreateTestBusinessControllerISpec extends UnitSpec with IntegrationBaseSpec with CreateTestBusinessFixtures {

  trait Test {

    val nino                   = "AA123456A"
    private val vendorClientId = "someId"
    private val businessId     = "someBusinessId"

    val downstreamUri = s"/test-support/business-details/$nino"

    def setupStubs(): StubMapping = {
      AuthStub.authorised()
      DownstreamStub.onSuccess(POST, downstreamUri, CREATED, ExampleCreateTestBusinessResponse.downstreamResponseJson)
    }

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/business/$nino")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

    val expectedResponseBody: String =
      s"""{
        |  "businessId": "$businessId",
        |  "links": [
        |    {
        |      "href": "/individuals/self-assessment-test-support/business/$nino/$businessId",
        |      "method": "DELETE",
        |      "rel": "delete-business"
        |    },
        |    {
        |      "href": "/individuals/business/details/$nino/list",
        |      "method": "GET",
        |      "rel": "list-businesses"
        |    },
        |    {
        |      "href": "/individuals/business/details/$nino/$businessId",
        |      "method": "GET",
        |      "rel": "self"
        |    }
        |  ]
        |}""".stripMargin

  }

  "Calling the create Business endpoint" should {
    "return a 201 status code" when {
      "a valid request is made" in new Test {

        val response: WSResponse = await(request().post(MinimalCreateTestBusinessRequest.mtdBusinessJson))

        response.status shouldBe CREATED
        response.header("X-CorrelationId") should not be empty
        response.json shouldBe Json.parse(expectedResponseBody)
      }
    }
    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String, requestBody: JsObject, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {
          override val nino: String = requestNino

          override def setupStubs(): StubMapping =
            AuthStub.authorised()

          val response: WSResponse = await(request().post(requestBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("BAD_NINO", MinimalCreateTestBusinessRequest.mtdBusinessJson, BAD_REQUEST, NinoFormatError),
        ("AA123456A", Json.obj("typeOfBusiness" -> "invalid business type"), BAD_REQUEST, TypeOfBusinessFormatError),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("firstAccountingPeriodStartDate" -> "not a valid date"),
          BAD_REQUEST,
          DateFormatError.withExtraPath("/firstAccountingPeriodStartDate")
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("accountingType" -> "not a valid accountingType"),
          BAD_REQUEST,
          AccountingTypeFormatError
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("commencementDate" -> LocalDate.now.plusYears(1)),
          BAD_REQUEST,
          RuleCommencementDateNotSupported
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("businessAddressPostcode" -> "not a valid postcode"),
          BAD_REQUEST,
          PostcodeFormatError
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj(
            "latencyDetails" -> Json
              .parse("""
                     |{
                     |  "latencyEndDate": "2024-12-12",
                     |  "taxYear1": "2022-23",
                     |  "latencyIndicator1":"not a valid indicator",
                     |  "taxYear2": "2023-24",
                     |  "latencyIndicator2":"Q"
                     |}
                     |""".stripMargin)),
          BAD_REQUEST,
          LatencyIndicatorFormatError.withExtraPath("/latencyDetails/latencyIndicator1")
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("businessAddressCountryCode" -> "GB"),
          BAD_REQUEST,
          MissingPostcodeError
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj("businessAddressCountryCode" -> "not a valid code"),
          BAD_REQUEST,
          CountryCodeFormatError
        ),
        (
          "AA123456A",
          JsObject.empty,
          BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj(
            "latencyDetails" -> Json
              .parse("""
                       |{
                       |  "latencyEndDate": "2024-12-12",
                       |  "taxYear1": "2-3",
                       |  "latencyIndicator1":"Q",
                       |  "taxYear2": "2023-24",
                       |  "latencyIndicator2":"Q"
                       |}
                       |""".stripMargin)),
          BAD_REQUEST,
          TaxYearFormatError.withExtraPath("/latencyDetails/taxYear1")
        ),
        (
          "AA123456A",
          MinimalCreateTestBusinessRequest.mtdBusinessJson ++ Json.obj(
            "latencyDetails" -> Json
              .parse("""
                       |{
                       |  "latencyEndDate": "2024-12-12",
                       |  "taxYear1": "2021-23",
                       |  "latencyIndicator1":"Q",
                       |  "taxYear2": "2023-24",
                       |  "latencyIndicator2":"Q"
                       |}
                       |""".stripMargin)),
          BAD_REQUEST,
          RuleTaxYearRangeInvalidError.withExtraPath("/latencyDetails/taxYear1")
        )
      )

      input.foreach(args => (validationErrorTest _).tupled(args))

    }

    "return downstream errors" when {
      def serviceError(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a $stubErrorCode error and status $stubErrorStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onError(POST, downstreamUri, stubErrorStatus, downstreamErrorBody(stubErrorCode))
          }

          val response: WSResponse = await(request().post(MinimalCreateTestBusinessRequest.mtdBusinessJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }

      val stubErrors = Seq(
        (BAD_REQUEST, "DUPLICATE_PROPERTY_BUSINESS", BAD_REQUEST, RulePropertyBusinessAddedError)
      )

      stubErrors.foreach(elem => (serviceError _).tupled(elem))
    }

  }

}

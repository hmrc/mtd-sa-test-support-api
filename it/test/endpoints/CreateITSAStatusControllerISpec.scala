/*
 * Copyright 2025 HM Revenue & Customs
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

import api.models.domain.TaxYear
import api.models.errors.*
import api.utils.JsonErrorValidators
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{AUTHORIZATION, BAD_REQUEST, NO_CONTENT}
import support.{IntegrationBaseSpec, UnitSpec}

class CreateITSAStatusControllerISpec extends UnitSpec with IntegrationBaseSpec with JsonErrorValidators {

  private def bodyWith(entries: JsValue*): JsObject = Json
    .parse(
      s"""
      |{
      |  "itsaStatusDetails": ${JsArray(entries)}
      |}
    """.stripMargin
    )
    .as[JsObject]

  private val itsaStatusDetail: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2021-03-23T16:02:34.039Z",
      |  "status": "No Status",
      |  "statusReason": "Sign up - return available",
      |  "businessIncome2YearsPrior": 34999.99
      |}
    """.stripMargin
  )

  "Calling the create Itsa Status endpoint" should {
    "return a 200 status code" when {
      "a valid request is made" in new Test {
        override val requestBody: JsObject = bodyWith(itsaStatusDetail)

        val response: WSResponse = await(request().post(requestBody))

        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: TaxYear,
                              requestBodyToTest: JsObject,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error and $requestNino" in new Test {
          override val nino: String          = requestNino
          override val taxYear: TaxYear      = requestTaxYear
          override val requestBody: JsObject = requestBodyToTest

          override def setupStubs(): StubMapping =
            AuthStub.authorised()

          val response: WSResponse = await(request().post(requestBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("BAD_NINO", TaxYear.fromMtd("2023-24"), bodyWith(itsaStatusDetail), BAD_REQUEST, NinoFormatError),
        ("AA123456A", TaxYear.fromMtd("2007-08"), bodyWith(itsaStatusDetail), BAD_REQUEST, TaxYearFormatError),
        (
          "AA123456A",
          TaxYear.fromMtd("2022-23"),
          bodyWith(itsaStatusDetail.update("/status", JsString("invalid_status"))),
          BAD_REQUEST,
          StatusFormatError.withExtraPath("/itsaStatusDetails/0/status")),
        (
          "AA123456A",
          TaxYear.fromMtd("2022-23"),
          bodyWith(itsaStatusDetail.update("/statusReason", JsString("invalid_status_reason"))),
          BAD_REQUEST,
          StatusReasonFormatError.withExtraPath("/itsaStatusDetails/0/statusReason")),
        (
          "AA123456A",
          TaxYear.fromMtd("2022-23"),
          bodyWith(itsaStatusDetail.update("/businessIncome2YearsPrior", JsNumber(-1))),
          BAD_REQUEST,
          BusinessIncome2YearsPriorFormatError.withExtraPath("/itsaStatusDetails/0/businessIncome2YearsPrior")),
        (
          "AA123456A",
          TaxYear.fromMtd("2022-23"),
          bodyWith(itsaStatusDetail.update("/submittedOn", JsString("2021-03-23T16:02:34"))),
          BAD_REQUEST,
          SubmittedOnFormatError.withExtraPath("/itsaStatusDetails/0/submittedOn")),
        (
          "AA123456A",
          TaxYear.fromMtd("2022-23"),
          bodyWith(itsaStatusDetail.removeProperty("/statusReason")),
          BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails/0/statusReason")),
        ("AA123456B", TaxYear.fromMtd("2022-23"), bodyWith(), BAD_REQUEST, RuleIncorrectOrEmptyBodyError.withExtraPath("/itsaStatusDetails"))
      )

      input.foreach((validationErrorTest _).tupled)
    }
  }

  trait Test {

    val nino: String           = "AA123456A"
    private val vendorClientId = "some_id"

    val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

    val requestBody: JsObject

    def setupStubs(): StubMapping = {
      val expectedDownstreamRequestBody: JsValue = Json.parse(
        """
          |{
          |  "itsaStatusDetails": [
          |    {
          |      "submittedOn": "2021-03-23T16:02:34.039Z",
          |      "status": "00",
          |      "statusReason": "00",
          |      "businessIncomePriorTo2Years": 34999.99
          |    }
          |  ]
          |}
        """.stripMargin
      )

      AuthStub.authorised()
      when(POST, s"/test-support/itsa-details/$nino/${taxYear.asTys}")
        .withRequestBody(expectedDownstreamRequestBody)
        .thenReturnNoContent()
    }

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/itsa-status/$nino/${taxYear.asMtd}")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

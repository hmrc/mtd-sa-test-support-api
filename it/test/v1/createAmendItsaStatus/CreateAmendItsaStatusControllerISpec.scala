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

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v1.createAmendItsaStatus.fixture.CreateAmendItsaStatusFixture.mtdRequestBodyJson

class CreateAmendItsaStatusControllerISpec extends IntegrationBaseSpec {

  private val allInvalidValuesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "itsaStatusDetails": [
      |    {
      |      "submittedOn": "2021-03-23",
      |      "status": "Status",
      |      "statusReason": "Sign up",
      |      "businessIncome2YearsPrior": 4500.233
      |    },
      |    {
      |      "submittedOn": "2021-03-23",
      |      "status": "Mandated",
      |      "statusReason": "declaration"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val allInvalidValuesRequestErrors: List[MtdError] = List(
    DuplicateSubmittedOnError,
    BusinessIncome2YearsPriorFormatError.withPath("/itsaStatusDetails/0/businessIncome2YearsPrior"),
    StatusFormatError.withPaths(List("/itsaStatusDetails/0/status", "/itsaStatusDetails/1/status")),
    StatusReasonFormatError.withPaths(List("/itsaStatusDetails/0/statusReason", "/itsaStatusDetails/1/statusReason")),
    SubmittedOnFormatError.withPaths(List("/itsaStatusDetails/0/submittedOn", "/itsaStatusDetails/1/submittedOn"))
  )

  private val wrappedErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "ignored",
    error = BadRequestError,
    errors = Some(allInvalidValuesRequestErrors)
  )

  "Calling the 'Create and Amend Test ITSA Status' endpoint" should {
    "return a 204 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = POST,
          uri = downstreamUri,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().post(mtdRequestBodyJson))

        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedError: MtdError,
                              expectedErrors: Option[ErrorWrapper]): Unit = {
        s"validation fails with ${expectedError.code} error" in new Test {
          override val nino: String    = requestNino
          override val taxYear: String = requestTaxYear

          val response: WSResponse = await(request().post(requestBody))

          response.status shouldBe expectedStatus
          response.json shouldBe expectedErrors.fold(expectedError.asJson)(Json.toJson(_))
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val validationInput: Seq[(String, String, JsValue, Int, MtdError, Option[ErrorWrapper])] = List(
        ("AA1123A", "2023-24", mtdRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
        ("AA123456A", "2023", mtdRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
        ("AA123456A", "2023-25", mtdRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
        ("AA123456A", "2023-24", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
        ("AA123456A", "2023-24", allInvalidValuesRequestBodyJson, BAD_REQUEST, BadRequestError, Some(wrappedErrors))
      )

      validationInput.foreach(validationErrorTest.tupled)

      def serviceErrorTest(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a code $stubErrorCode error and status $stubErrorStatus" in new Test {
          override def setupStubs(): Unit = DownstreamStub.onError(
            method = POST,
            uri = downstreamUri,
            errorStatus = stubErrorStatus,
            errorBody = downstreamErrorBody(stubErrorCode)
          )

          val response: WSResponse = await(request().post(mtdRequestBodyJson))
          response.json shouldBe expectedError.asJson
          response.status shouldBe expectedStatus
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val serviceInput: Seq[(Int, String, Int, MtdError)] = List(
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      serviceInput.foreach(serviceErrorTest.tupled)
    }
  }

  private trait Test {
    val nino: String    = "AA123456A"
    val taxYear: String = "2023-24"

    val downstreamUri: String = s"/test-support/itsa-details/$nino/23-24"

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest(s"/itsa-status/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7")
        )
    }

  }

}

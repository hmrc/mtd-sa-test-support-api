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

import api.models.errors.{BusinessIdFormatError, MtdError, NinoFormatError, NotFoundError}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, NO_CONTENT}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class DeleteTestBusinessControllerISpec extends IntegrationBaseSpec {

  "Deleting a test business" must {
    "return 204 status code" when {
      "a successful valid request is made" in new Test {
        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }
    }
    "return validation errors according to the spec" when {
      def validationErrorTest(inputNino: String, inputBusinessId:String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override def setupStubs(): StubMapping =
            AuthStub.authorised()

          override def request(): WSRequest = {
            setupStubs()
            buildRequest(s"/business/$inputNino/$inputBusinessId")
              .withHttpHeaders(
                (ACCEPT, "application/vnd.hmrc.1.0+json"),
                (AUTHORIZATION, "Bearer 123"), // some bearer token
                ("X-Client-Id", "some_id")
              )
          }

          val response: WSResponse = await(request().delete())

          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("invalid_nino", "XAIS12345678910", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "invalidBusinessId", BAD_REQUEST, BusinessIdFormatError)
      )

      input.foreach { case (nino, businessId, status, error) =>
        validationErrorTest(nino, businessId, status, error)
      }
    }
    "return the downstream error" when {
      def serviceError(downstreamErrorStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"downstream returns a $downstreamErrorCode error and status $downstreamErrorStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onError(DELETE, downstreamUri, Seq.empty, downstreamErrorStatus, downstreamErrorBody(downstreamErrorCode))
          }

          val response: WSResponse = await(request().delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }

      val stubErrors = Seq(
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
      )

      stubErrors.foreach(elem => (serviceError _).tupled(elem))
    }
  }

  trait Test {

    val nino       = "AA123456A"
    val businessId = "XAIS12345678910"

    val mtdUri        = s"/business/$nino/$businessId"
    val downstreamUri = s"/test-support/business-details/$nino/$businessId"

    def setupStubs(): StubMapping = {
      AuthStub.authorised()
      DownstreamStub.onSuccess(DELETE, downstreamUri, NO_CONTENT, JsObject.empty)
    }

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"), // some bearer token
          ("X-Client-Id", "some_id")
        )
    }

  }

}

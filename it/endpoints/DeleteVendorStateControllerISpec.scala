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

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, SERVICE_UNAVAILABLE}
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class DeleteVendorStateControllerISpec extends IntegrationBaseSpec {

  "Calling the delete vendor state endpoint" should {
    "return a 204 status code" when {
      "a valid request is made" in new Test {

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }

      "a valid request with nino is made" in new Test {
        override val mtdQueryParams: Seq[(String, String)]        = Seq("nino" -> nino)
        override val downstreamQueryParams: Seq[(String, String)] = Seq("taxableEntityId" -> nino)

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }
    }

    "return a stub error" when {
      def serviceError(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a $stubErrorCode error and status $stubErrorStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onError(DELETE, downstreamUri, Seq.empty, stubErrorStatus, downstreamErrorBody(stubErrorCode))
          }

          val response: WSResponse = await(request().delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }

      val stubErrors = Seq(
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      stubErrors.foreach(elem => (serviceError _).tupled(elem))
    }
  }

  trait Test {
    val nino           = "AA123456A"
    val vendorClientId = "some_id"

    val mtdUri                                = "/vendor-state"
    val mtdQueryParams: Seq[(String, String)] = Seq.empty

    val downstreamUri                                = s"/test-support/vendor-state/$vendorClientId"
    val downstreamQueryParams: Seq[(String, String)] = Seq.empty

    def setupStubs(): StubMapping = {
      AuthStub.authorised()
      DownstreamStub.onSuccess(DELETE, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
    }

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .addQueryStringParameters(mtdQueryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"), // some bearer token
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

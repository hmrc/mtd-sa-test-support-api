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

package v1.deleteStatefulTestData

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*

class DeleteStatefulTestDataControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Delete Stateful Test Data' endpoint" should {
    "return a 204 status code" when {
      "a valid request is made with query parameter nino" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DELETE,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().delete())

        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "a valid request is made without query parameter nino" in new Test {
        override val mtdQueryParams: Seq[(String, String)]        = Seq.empty
        override val downstreamQueryParams: Seq[(String, String)] = Seq.empty

        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DELETE,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().delete())

        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation fails with FORMAT_NINO error" in new Test {
        override val mtdQueryParams: Seq[(String, String)] = Seq("nino" -> "AA1123A")

        val response: WSResponse = await(request().delete())

        response.status shouldBe BAD_REQUEST
        response.json shouldBe NinoFormatError.asJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      def serviceErrorTest(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a code $stubErrorCode error and status $stubErrorStatus" in new Test {
          override def setupStubs(): Unit = DownstreamStub.onError(
            method = DELETE,
            uri = downstreamUri,
            queryParams = downstreamQueryParams,
            errorStatus = stubErrorStatus,
            errorBody = downstreamErrorBody(stubErrorCode)
          )

          val response: WSResponse = await(request().delete())
          response.json shouldBe expectedError.asJson
          response.status shouldBe expectedStatus
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val serviceInput: Seq[(Int, String, Int, MtdError)] = List(
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      serviceInput.foreach(serviceErrorTest.tupled)
    }
  }

  private trait Test {
    private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
    private val nino: String           = "AA123456A"

    val mtdQueryParams: Seq[(String, String)] = Seq("nino" -> nino)

    val downstreamUri: String                        = s"/test-support/vendor-state/$vendorClientId"
    val downstreamQueryParams: Seq[(String, String)] = Seq("taxableEntityId" -> nino)

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest("/vendor-state")
        .addQueryStringParameters(mtdQueryParams*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

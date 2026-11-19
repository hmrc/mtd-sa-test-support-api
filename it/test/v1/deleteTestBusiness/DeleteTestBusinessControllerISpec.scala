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

package v1.deleteTestBusiness

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*

class DeleteTestBusinessControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Delete a Test Business' endpoint" should {
    "return a 204 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DELETE,
          uri = downstreamUri,
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
      def validationErrorTest(requestNino: String, requestBusinessId: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"validation fails with ${expectedError.code} error" in new Test {
          override val nino: String       = requestNino
          override val businessId: String = requestBusinessId

          val response: WSResponse = await(request().delete())

          response.status shouldBe expectedStatus
          response.json shouldBe expectedError.asJson
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val validationInput: Seq[(String, String, Int, MtdError)] = List(
        ("AA1123A", "X0IS12345678901", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "X0IS123", BAD_REQUEST, BusinessIdFormatError)
      )

      validationInput.foreach(validationErrorTest.tupled)

      "stub returns a code NOT_FOUND error and status 404" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onError(
          method = DELETE,
          uri = downstreamUri,
          errorStatus = NOT_FOUND,
          errorBody = downstreamErrorBody("NOT_FOUND")
        )

        val response: WSResponse = await(request().delete())

        response.status shouldBe NOT_FOUND
        response.json shouldBe NotFoundError.asJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }

  private trait Test {
    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"

    val downstreamUri: String = s"/test-support/business-details/$nino/$businessId"

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest(s"/business/$nino/$businessId")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7")
        )
    }

  }

}

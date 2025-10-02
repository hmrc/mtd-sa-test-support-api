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

import api.models.errors.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.*
import play.api.libs.json.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}
import support.IntegrationBaseSpec

class RestoreCheckpointControllerISpec extends IntegrationBaseSpec {

  "Calling the restore checkpoint endpoint" should {
    "return a 201 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(POST, downstreamUri, Seq.empty, CREATED, JsObject.empty)
        }

        val response: WSResponse = await(request().post(JsNull))

        response.status shouldBe CREATED
        response.json shouldBe expectedResponseBody
        response.header("X-CorrelationId") should not be empty
      }
    }
    "return validation errors according to the spec" when {
      def validationErrorTest(checkpointIdentifier: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {
          override val checkpointId: String = checkpointIdentifier

          override def setupStubs(): StubMapping =
            AuthStub.authorised()

          val response: WSResponse = await(request().post(JsNull))

          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("invalid_checkpoint_id", BAD_REQUEST, CheckpointIdFormatError)
      )

      input.foreach { case (id, status, error) =>
        validationErrorTest(id, status, error)
      }
    }

    "return a mtd error corresponding to the received downstream error" when {
      def serviceError(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a $stubErrorCode error and status $stubErrorStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onError(POST, downstreamUri, Seq.empty, stubErrorStatus, downstreamErrorBody(stubErrorCode))
          }

          val response: WSResponse = await(request().post(JsNull))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }

      val stubErrors = Seq(
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
      )

      stubErrors.foreach { case (stubErrorStatus, stubErrorCode, expectedStatus, expectedError) =>
        serviceError(stubErrorStatus, stubErrorCode, expectedStatus, expectedError)
      }
    }

  }

  trait Test {
    val vendorClientId = "some_client_id"
    val checkpointId   = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

    val expectedResponseBody: JsValue = Json.parse(
      s"""
         |{
         | "links": [
         |   {
         |     "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId",
         |     "rel": "delete-checkpoint",
         |     "method": "DELETE"
         |   }
         | ]
         |}""".stripMargin
    )

    val downstreamUri = s"/test-support/vendor-state/$vendorClientId/checkpoints/$checkpointId/restore"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/vendor-state/checkpoints/$checkpointId/restore")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

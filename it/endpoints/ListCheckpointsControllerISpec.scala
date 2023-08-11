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
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import support.IntegrationBaseSpec

class ListCheckpointsControllerISpec extends IntegrationBaseSpec {

  "Listing checkpoints" when {
    "a request is processed successfully with a nino" should {
      "return a 200 response" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(GET, downstreamPath, Seq("taxableEntityId" -> nino), 200, downstreamResponseWithNino)
        }

        override def request(): WSRequest = super.request().withQueryStringParameters("nino" -> nino)

        val response: WSResponse = await(request().get())

        response.status shouldBe 200
        response.json shouldBe expectedMtdResponseWithNino
        response.header("X-CorrelationId") should not be empty
      }
    }
    "a request is processed successfully without a nino" should {
      "return a 200 response" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(GET, downstreamPath, Seq.empty, 200, downstreamResponseWithoutNino)
        }

        val response: WSResponse = await(request().get())

        response.status shouldBe 200
        response.json shouldBe expectedMtdResponseWithoutNino
        response.header("X-CorrelationId") should not be empty
      }
    }
    "processing of a request fails with validation errors" should {
      def validationErrorTest(requestNino: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {
          override def setupStubs(): StubMapping =
            AuthStub.authorised()

          override def request(): WSRequest = super.request().withQueryStringParameters("nino" -> requestNino)

          val response: WSResponse = await(request().get())

          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("invalid_nino", BAD_REQUEST, NinoFormatError)
      )

      input.foreach { case (nino, status, error) =>
        validationErrorTest(nino, status, error)
      }
    }
    "return an mtd error corresponding to the received downstream error" when {
      def serviceError(stubErrorStatus: Int, stubErrorCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"stub returns a $stubErrorCode error and status $stubErrorStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onError(GET, downstreamPath, Seq.empty, stubErrorStatus, downstreamErrorBody(stubErrorCode))
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }

      val stubErrors = Seq(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SEVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      stubErrors.foreach { case (stubErrorStatus, stubErrorCode, expectedStatus, expectedError) =>
        serviceError(stubErrorStatus, stubErrorCode, expectedStatus, expectedError)
      }
    }

    trait Test {
      val vendorClientId              = "some_client_id"
      val checkpointId                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
      val nino                        = "AA123456A"
      val checkpointCreationTimestamp = "2019-01-01T00:00:00.000Z"

      val downstreamResponseWithNino: JsObject = Json.obj(
        "checkpoints" -> Json.arr(
          Json.obj("checkpointId" -> checkpointId, "nino" -> nino, "checkpointCreationTimestamp" -> checkpointCreationTimestamp)))

      val downstreamResponseWithoutNino: JsObject =
        Json.obj("checkpoints" -> Json.arr(Json.obj("checkpointId" -> checkpointId, "checkpointCreationTimestamp" -> checkpointCreationTimestamp)))

      val expectedMtdResponseWithNino: JsValue = Json.parse(s"""
           |{
           |  "checkpoints": [
           |    {
           |      "checkpointId": "$checkpointId",
           |      "nino": "$nino",
           |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp",
           |      "links": [
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/?nino=$nino",
           |          "method": "POST",
           |          "rel": "create-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId",
           |          "method": "DELETE",
           |          "rel": "delete-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId/restore",
           |          "method": "POST",
           |          "rel": "restore-checkpoint"
           |        }
           |      ]
           |    }
           |  ]
           |}
           |""".stripMargin)

      val expectedMtdResponseWithoutNino: JsValue = Json.parse(s"""
           |{
           |  "checkpoints": [
           |    {
           |      "checkpointId": "$checkpointId",
           |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp",
           |      "links": [
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId",
           |          "method": "DELETE",
           |          "rel": "delete-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId/restore",
           |          "method": "POST",
           |          "rel": "restore-checkpoint"
           |        }
           |      ]
           |    }
           |  ]
           |}
           |""".stripMargin)

      val mtdPath = "/vendor-state/checkpoints"

      val downstreamPath = s"/test-support/vendor-state/$vendorClientId"

      def setupStubs(): StubMapping

      def request(): WSRequest = {
        setupStubs()
        buildRequest(mtdPath)
          .withHttpHeaders(
            (ACCEPT, "application/vnd.hmrc.1.0+json"),
            (AUTHORIZATION, "Bearer 123"),
            ("X-Client-Id", vendorClientId)
          )
      }

    }
  }

}

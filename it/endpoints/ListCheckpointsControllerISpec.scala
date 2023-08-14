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
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import support.IntegrationBaseSpec

class ListCheckpointsControllerISpec extends IntegrationBaseSpec {

  "Listing checkpoints" when {
    "querying by nino" when {
      "a request is processed successfully" should {
        "return a 200 response" in new Test {
          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onSuccess(GET, downstreamPath, Seq("taxableEntityId" -> nino), 200, listWithNinoDownstreamResponse)
          }

          override def request(): WSRequest = super.request().withQueryStringParameters("nino" -> nino)

          val response: WSResponse = await(request().get())

          response.status shouldBe 200
          response.json shouldBe expectedListWithNinoMtdResponse
          response.header("X-CorrelationId") should not be empty
        }
      }
    }
    "not querying by nino" when {
      "a request is processed successfully" should {
        "return a 200 response" in new Test {
          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onSuccess(GET, downstreamPath, Seq.empty, 200, listDownstreamResponse)
          }

          val response: WSResponse = await(request().get())

          response.status shouldBe 200
          response.json shouldBe expectedListWithoutNinoMtdResponse
          response.header("X-CorrelationId") should not be empty
        }
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

    trait Test {
      val vendorClientId               = "some_client_id"
      val checkpointId1                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
      val checkpointId2                = "b2e4050e-fbbc-47a8-d5b4-65d9f015c253"
      val nino                         = "AA123456A"
      val checkpointCreationTimestamp1 = "2019-01-01T00:00:00.000Z"
      val checkpointCreationTimestamp2 = "2019-01-02T00:00:00.000Z"

      protected val listWithNinoDownstreamResponse: JsValue = Json.parse(
        s"""
           |{
           |  "checkpoints": [
           |    {
           |       "checkpointId": "$checkpointId1",
           |       "taxableEntityId": "$nino",
           |       "checkpointCreationTimestamp": "$checkpointCreationTimestamp1"
           |     }
           |  ]
           |}
           |""".stripMargin
      )

      protected val listDownstreamResponse: JsValue = Json.parse(
        s"""
           |{
           |  "checkpoints": [
           |    {
           |       "checkpointId": "$checkpointId1",
           |       "taxableEntityId": "$nino",
           |       "checkpointCreationTimestamp": "$checkpointCreationTimestamp1"
           |     },
           |     {
           |       "checkpointId": "$checkpointId2",
           |       "checkpointCreationTimestamp": "$checkpointCreationTimestamp2"
           |      }
           |  ]
           |}
           |""".stripMargin
      )

      val expectedListWithNinoMtdResponse: JsValue = Json.parse(s"""
           |{
           |  "checkpoints": [
           |    {
           |      "checkpointId": "$checkpointId1",
           |      "nino": "$nino",
           |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp1",
           |      "links": [
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/?nino=$nino",
           |          "method": "POST",
           |          "rel": "create-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId1",
           |          "method": "DELETE",
           |          "rel": "delete-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId1/restore",
           |          "method": "POST",
           |          "rel": "restore-checkpoint"
           |        }
           |      ]
           |    }
           |  ]
           |}
           |""".stripMargin)

      val expectedListWithoutNinoMtdResponse: JsValue = Json.parse(s"""
           |{
           |  "checkpoints": [
           |    {
           |      "checkpointId": "$checkpointId1",
           |      "nino": "$nino",
           |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp1",
           |      "links": [
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/?nino=$nino",
           |          "method": "POST",
           |          "rel": "create-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId1",
           |          "method": "DELETE",
           |          "rel": "delete-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId1/restore",
           |          "method": "POST",
           |          "rel": "restore-checkpoint"
           |        }
           |      ]
           |    },
           |    {
           |      "checkpointId": "$checkpointId2",
           |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp2",
           |      "links": [
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId2",
           |          "method": "DELETE",
           |          "rel": "delete-checkpoint"
           |        },
           |        {
           |          "href": "/individuals/self-assessment-test-support/vendor-state/checkpoints/$checkpointId2/restore",
           |          "method": "POST",
           |          "rel": "restore-checkpoint"
           |        }
           |      ]
           |    }
           |  ]
           |}
           |""".stripMargin)

      val mtdPath = "/vendor-state/checkpoints"

      val downstreamPath = s"/test-support/vendor-state/$vendorClientId/checkpoints"

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

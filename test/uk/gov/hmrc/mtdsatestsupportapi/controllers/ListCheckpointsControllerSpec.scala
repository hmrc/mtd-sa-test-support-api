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

package uk.gov.hmrc.mtdsatestsupportapi.controllers

import api.controllers.*
import api.hateoas.*
import api.hateoas.Method.GET
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.libs.json.*
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockListCheckpointsRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockListCheckpointsService
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockListCheckpointsValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.{Business, TypeOfBusiness}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.{ListCheckpointsRawData, ListCheckpointsRequest}
import uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints.{Checkpoint, ListCheckpointsHateoasData, ListCheckpointsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListCheckpointsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListCheckpointsRequestParser
    with MockListCheckpointsService
    with MockListCheckpointsValidator
    with ControllerSpecHateoasSupport {

  "ListCheckpointsController" when {
    "X-Client-Id header is present" when {
      "handleRequest is invoked" should {
        "return a 200 action if the request is processed successfully" in new Test {
          override def callController(): Future[Result] =
            controller.handleRequest(Some(nino))(
              fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_vendor_id"))

          private val business = Business(TypeOfBusiness.`self-employment`, Some("RCDTS"), None, None, None,
            None, None, None, None, None, None, None, None, None, None)
          private val responseData = ListCheckpointsResponse(Seq(business))
          private val testHateoasLink = Link(href = "/foo/bar", method = GET, rel = "test-relationship")

          MockListCheckpointsRequestParser
            .parseRequest(rawDataWithNino)
            .returns(Right(ListCheckpointsRequest(vendorClientId, Some(Nino(nino)))))

          MockListCheckpointsService
            .listCheckpoints(requestDataWithNino)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseDataWithNino))))

          val expectedResponseBody: JsValue = Json.parse(s"""
               |{
               |  "checkpoints": [
               |    {
               |      "checkpointId": "$checkpointId",
               |      "nino": "$nino",
               |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp",
               |      "links": [
               |        {
               |          "href": "/individuals/vendor-state/checkpoints/vendor-state/checkpoints/?nino=$nino",
               |          "method": "POST",
               |          "rel": "create-checkpoint"
               |        },
               |        {
               |          "href": "/individuals/vendor-state/checkpoints/vendor-state/checkpoints/some_checkpoint_id",
               |          "method": "DELETE",
               |          "rel": "delete-checkpoint"
               |        },
               |        {
               |          "href": "/individuals/vendor-state/checkpoints/vendor-state/checkpoints/some_checkpoint_id/restore",
               |          "method": "POST",
               |          "rel": "restore-checkpoint"
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          runOkTest(expectedStatus = 200, Some(expectedResponseBody))
        }
        "return the mtd errors if a request has failed during validation" in new Test {
          override def callController(): Future[Result] =
            controller.handleRequest(Some(nino))(
              fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_vendor_id"))

          MockListCheckpointsRequestParser
            .parseRequest(rawDataWithNino)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

          runErrorTest(NinoFormatError)
        }
        "return the mtd errors if a request has failed downstream" in new Test {
          override def callController(): Future[Result] =
            controller.handleRequest(Some(nino))(
              fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_vendor_id"))

          MockListCheckpointsRequestParser
            .parseRequest(rawDataWithNino)
            .returns(Right(ListCheckpointsRequest(vendorClientId, Some(Nino(nino)))))

          MockListCheckpointsService
            .listCheckpoints(requestDataWithNino)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(NotFoundError)
        }
      }
    }
    "X-Client-Id header is not present" should {
      "return a 500" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(Some(nino))(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
    }
  }

  trait Test extends ControllerTest {

    protected val checkpointId                = "some_checkpoint_id"
    protected val checkpointCreationTimestamp = "2019-01-01T00:00:00.000Z"

    protected val nino           = "TC663795B"
    protected val vendorClientId = "some_vendor_id"
    protected val correlationId  = "X-123"

    private val checkpoint = Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp)

    protected val rawDataWithNino: ListCheckpointsRawData     = ListCheckpointsRawData(vendorClientId, Some(nino))
    protected val requestDataWithNino: ListCheckpointsRequest = ListCheckpointsRequest(vendorClientId, Some(Nino(nino)))

    protected val responseDataWithNino: ListCheckpointsResponse[Checkpoint] = ListCheckpointsResponse(
      Seq(Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp)))

    protected val hateoasResponse: ListCheckpointsResponse[HateoasWrapper[Checkpoint]] = ListCheckpointsResponse(
      Seq(HateoasWrapper(checkpoint, hateoaslinks)))

    protected val controller = new ListCheckpointsController(
      mockEnrolmentsAuthService,
      cc,
      mockListCheckpointsRequestParser,
      mockListCheckpointsService,
      mockIdGenerator,
      new HateoasFactory(mockAppConfig))

  }

}

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

import api.controllers.{ControllerBaseSpec, ControllerSpecHateoasSupport, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.mocks.MockIdGenerator
import api.mocks.services.MockAuthService
import api.models.domain.{CheckpointId, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockCreateCheckpointRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockCreateCheckpointService
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.{CreateCheckpointRawData, CreateCheckpointRequest}
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.{CreateCheckpointHateoasData, CreateCheckpointResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateCheckpointControllerSpec
    extends ControllerBaseSpec
    with ControllerSpecHateoasSupport
    with ControllerTestRunner
    with MockCreateCheckpointService
    with MockCreateCheckpointRequestParser
    with MockAuthService
    with MockHateoasFactory
    with MockIdGenerator {

  trait Test extends ControllerTest {
    protected val vendorClientId = "some_id"

    val checkpointId = "someCheckpointId"

    // WLOG (as value not touched)
    val maybeNino: Option[String] = Some(nino)

    val rawData: CreateCheckpointRawData     = CreateCheckpointRawData(vendorClientId, maybeNino)
    val requestData: CreateCheckpointRequest = CreateCheckpointRequest(vendorClientId, Nino(nino))
    val response: CreateCheckpointResponse   = CreateCheckpointResponse(CheckpointId(checkpointId))

    val controller = new CreateCheckpointController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      idGenerator = mockIdGenerator)

    protected val hateoasResponse: JsObject = Json.obj("checkpointId" -> checkpointId) ++ hateoaslinksJson

  }

  "handleRequest" should {
    "return CREATED with the checkpointId" when {
      "a valid request is processed successfully" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(maybeNino)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateCheckpointRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateCheckpointService
          .createCheckpoint(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateCheckpointHateoasData(Nino(nino), CheckpointId(checkpointId)))
          .returns(HateoasWrapper(response, hateoaslinks))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(hateoasResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(maybeNino)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateCheckpointRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)

      }

      "the service returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(maybeNino)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateCheckpointRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateCheckpointService
          .createCheckpoint(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }

    "return an InternalServerError" when {
      "the request is missing an X-Client-Id header" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(maybeNino)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
    }
  }

}

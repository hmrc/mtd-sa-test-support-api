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

import api.controllers._
import api.mocks.MockIdGenerator
import api.mocks.services.MockAuthService
import api.models.domain.CheckpointId
import api.models.errors.{CheckpointIdFormatError, ErrorWrapper, InternalError, NotFoundError}
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.mvc.Result
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockDeleteCheckpointRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockDeleteCheckpointService
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteCheckpoint.{DeleteCheckpointRawData, DeleteCheckpointRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCheckpointControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with UnitSpec
    with MockDeleteCheckpointRequestParser
    with MockDeleteCheckpointService
    with MockAuthService
    with MockIdGenerator {

  trait Test extends ControllerTest {
    protected val vendorClientId = "some_id"
    protected val checkpointId   = "someCheckpointId"

    val rawData: DeleteCheckpointRawData     = DeleteCheckpointRawData(vendorClientId, checkpointId)
    val requestData: DeleteCheckpointRequest = DeleteCheckpointRequest(vendorClientId, CheckpointId(checkpointId))

    val controller = new DeleteCheckpointController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockDeleteCheckpointRequestParser,
      service = mockDeleteCheckpointService,
      idGenerator = mockIdGenerator)
  }

  "DeleteCheckpointController" when {
    "X-Client-Id header is present" when {
      "a valid request is processed successfully" should {
        "return a success 204 result" in new Test {
          override protected def callController(): Future[Result] =
            controller.handleRequest(checkpointId)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

          MockDeleteCheckpointRequestParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockDeleteCheckpointService
            .deleteCheckpoint(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          runOkTest(expectedStatus = NO_CONTENT)
        }
      }
      "a request is unsuccessful due to failing parser validation" should {
        "return the corresponding error" in new Test {
          override protected def callController(): Future[Result] =
            controller.handleRequest(checkpointId)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

          MockDeleteCheckpointRequestParser
            .parseRequest(rawData)
            .returns(Left(ErrorWrapper(correlationId, CheckpointIdFormatError, None)))

          runErrorTest(CheckpointIdFormatError)
        }
      }
      "the service returns an error during processing" should {
        "return the corresponding error" in new Test {
          override protected def callController(): Future[Result] =
            controller.handleRequest(checkpointId)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

          MockDeleteCheckpointRequestParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockDeleteCheckpointService
            .deleteCheckpoint(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(NotFoundError)
        }
      }
    }

    "X-Client-Id header is not present" should {
      "return an internal server error result" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(checkpointId)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
    }
  }

}

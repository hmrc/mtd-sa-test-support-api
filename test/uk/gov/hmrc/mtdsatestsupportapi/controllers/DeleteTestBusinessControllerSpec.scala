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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.Nino
import api.models.errors.{BusinessIdFormatError, ErrorWrapper, InternalError, NinoFormatError, NotFoundError}
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockDeleteTestBusinessRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockDeleteTestBusinessService
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.{DeleteTestBusinessRawData, DeleteTestBusinessRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteTestBusinessControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteTestBusinessRequestParser
    with MockDeleteTestBusinessService {

  trait Test extends ControllerTest {
    val nino           = "AA123456A"
    val businessId     = "XAIS12345678910"
    val vendorClientId = "some_id"

    val rawData: DeleteTestBusinessRawData     = DeleteTestBusinessRawData(nino, businessId)
    val requestData: DeleteTestBusinessRequest = DeleteTestBusinessRequest(Nino(nino), businessId)

    val controller = new DeleteTestBusinessController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockParser,
      service = mockService,
      idGenerator = mockIdGenerator
    )

  }

  "DeleteTestBusinessController" must {
    "return 500 Internal server error" when {
      "the X-Client-Id header is missing" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, businessId)(fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
    }
    "return the correct error" when {
      "the parser validation rejects the nino" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, businessId)(
            fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockDeleteTestBusinessRequestParser.parseRequest(rawData).returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }
      "the parser validation rejects the businessId" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, businessId)(
            fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockDeleteTestBusinessRequestParser.parseRequest(rawData).returns(Left(ErrorWrapper(correlationId, BusinessIdFormatError, None)))

        runErrorTest(BusinessIdFormatError)
      }
      "the service returns a 404 error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, businessId)(
            fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockDeleteTestBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockDeleteTestBusinessService.deleteTestBusiness(requestData).returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
    "return NO_CONTENT" when {
      "valid parameters are provided" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, businessId)(
            fakeRequestWithHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockDeleteTestBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockDeleteTestBusinessService.deleteTestBusiness(requestData).returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT, maybeExpectedResponseBody = None)
      }
    }

  }

}

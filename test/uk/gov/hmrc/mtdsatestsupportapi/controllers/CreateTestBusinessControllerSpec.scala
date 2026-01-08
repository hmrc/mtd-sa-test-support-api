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

package uk.gov.hmrc.mtdsatestsupportapi.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createTestBusiness.CreateTestBusinessResponse
import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, InternalError, NinoFormatError, RulePropertyBusinessAddedError}
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateTestBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockCreateTestBusinessRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockCreateTestBusinessService
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createTestBusiness.{CreateTestBusinessRawData, CreateTestBusinessRequest}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateTestBusinessControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with CreateTestBusinessFixtures
    with MockCreateTestBusinessRequestParser
    with MockCreateTestBusinessService
    {

  trait Test extends ControllerTest {
    val nino           = "AA123456A"
    val vendorClientId = "someId"
    val businessId: String = ExampleCreateTestBusinessResponse.businessId

    val rawData: CreateTestBusinessRawData     = CreateTestBusinessRawData(nino, MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson)
    val requestData: CreateTestBusinessRequest = CreateTestBusinessRequest(Nino(nino), MinimalCreateTestBusinessRequest.SelfEmployment.business)
    val response: CreateTestBusinessResponse = ExampleCreateTestBusinessResponse.response

    val controller = new CreateTestBusinessController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockParser,
      service = mockService,
      idGenerator = mockIdGenerator
    )
  }

  "CreateTestBusinessController" must {
    "return 201 Created" when {
      "a valid request is sent" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest
              .withBody(MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson)
              .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateTestBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockCreateTestBusinessService.CreateTestBusiness(requestData).returns(Future.successful(Right(ResponseWrapper(correlationId, response))))
        
        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(
          Json.obj(
            "businessId" -> businessId
          )
        )
        )
      }
    }
    "return error according to spec" when {
      "the request lacks an X-Client-Id header" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest.withBody(MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
      "the validator returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest
              .withBody(MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson)
              .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateTestBusinessRequestParser.parseRequest(rawData).returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }
      "the service returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest
              .withBody(MinimalCreateTestBusinessRequest.SelfEmployment.mtdBusinessJson)
              .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateTestBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockCreateTestBusinessService
          .CreateTestBusiness(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RulePropertyBusinessAddedError))))

        runErrorTest(RulePropertyBusinessAddedError)
      }
    }

  }

}

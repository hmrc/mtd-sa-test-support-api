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
import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, InternalError, NinoFormatError, RulePropertyBusinessAddedError}
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockCreateBusinessRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockCreateBusinessService
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness.{CreateBusinessRawData, CreateBusinessRequest}
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createBusiness.CreateBusinessHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateBusinessControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with CreateBusinessFixtures
    with MockCreateBusinessRequestParser
    with MockCreateBusinessService
    with MockHateoasFactory
    with ControllerSpecHateoasSupport {

  trait Test extends ControllerTest {
    val nino           = "AA123456A"
    val vendorClientId = "someId"
    val businessId     = ExampleCreateBusinessResponse.businessId

    val rawData: CreateBusinessRawData     = CreateBusinessRawData(nino, MinimalCreateBusinessRequest.mtdBusinessJson)
    val requestData: CreateBusinessRequest = CreateBusinessRequest(Nino(nino), MinimalCreateBusinessRequest.business)
    val response                           = ExampleCreateBusinessResponse.response



    val controller = new CreateBusinessController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockParser,
      service = mockService,
      idGenerator = mockIdGenerator,
      hateoasFactory = mockHateoasFactory,
    )

    protected val hateoasResponse: JsObject = Json.obj("businessId" -> businessId) ++ hateoaslinksJson

  }

  "CreateBusinessController" must {
    "return 201 Created" when {
      "a valid request is sent" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest.withBody(MinimalCreateBusinessRequest.mtdBusinessJson).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockCreateBusinessService.createBusiness(requestData).returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateBusinessHateoasData(Nino(nino), businessId))
          .returns(HateoasWrapper(response, hateoaslinks))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(hateoasResponse))
      }
    }
    "return error according to spec" when {
      "the request lacks an X-Client-Id header" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest.withBody(MinimalCreateBusinessRequest.mtdBusinessJson).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
      "the validator returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest.withBody(MinimalCreateBusinessRequest.mtdBusinessJson).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateBusinessRequestParser.parseRequest(rawData).returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }
      "the service returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino)(
            fakeRequest.withBody(MinimalCreateBusinessRequest.mtdBusinessJson).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> vendorClientId))

        MockCreateBusinessRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockCreateBusinessService
          .createBusiness(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RulePropertyBusinessAddedError))))

        runErrorTest(RulePropertyBusinessAddedError)
      }
    }

  }

}

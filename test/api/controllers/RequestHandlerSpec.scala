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

package api.controllers

import api.config.{AppConfig, MockAppConfig}
import api.controllers.validators.Validator
import api.models.auth.UserDetails
import api.models.errors.{ErrorWrapper, InternalError, MtdError, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import api.utils.{MockIdGenerator, UnitSpec}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.{FakeRequest, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class RequestHandlerSpec extends UnitSpec with MockIdGenerator with Status with HeaderNames with ResultExtractors with MockAppConfig {

  private implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  private val successResponseJson = Json.obj("result" -> "SUCCESS!")
  private val successCode         = ACCEPTED

  private val generatedCorrelationId = "generatedCorrelationId"
  private val serviceCorrelationId   = "serviceCorrelationId"

  MockedIdGenerator.getCorrelationId.returns(generatedCorrelationId).anyNumberOfTimes()

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "SomeController", endpointName = "someEndpoint")

  private val versionHeader = HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+json"

  implicit val hc: HeaderCarrier   = HeaderCarrier()
  implicit val ctx: RequestContext = RequestContext.from(mockIdGenerator, endpointLogContext)

  private val userDetails = UserDetails("Individual", Some("agentReferenceNumber"))

  implicit val userRequest: UserRequest[AnyContent] = {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)
    UserRequest[AnyContent](userDetails, fakeRequest)
  }

  implicit val appConfig: AppConfig = mockAppConfig
  private val mockService           = mock[DummyService]

  private def service =
    (mockService.service(_: Input.type)(_: RequestContext, _: ExecutionContext)).expects(Input, *, *).anyNumberOfTimes()

  case object Input

  case object Output {
    implicit val writes: OWrites[Output.type] = _ => successResponseJson
  }

  trait DummyService {
    def service(input: Input.type)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Output.type]]
  }

  private val successValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Valid(Input)
  }

  private val singleErrorValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Invalid(List(NinoFormatError))
  }

  private val successRequestHandler =
    RequestHandler
      .withValidator(successValidatorForRequest)
      .withService(mockService.service)

  "RequestHandler" when {
    "given a request" should {
      "return the correct response" in {
        val requestHandler = successRequestHandler.withPlainJsonResult(successCode)

        service.returns(Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output))))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe successResponseJson
        header(header = "X-CorrelationId", of = result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe successCode
      }

      "return no content if required" in {
        val requestHandler = successRequestHandler.withNoContentResult()

        service.returns(Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output))))

        val result = requestHandler.handleRequest()

        contentAsString(result) shouldBe ""
        header(header = "X-CorrelationId", of = result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NO_CONTENT
      }
    }

    "a request fails with validation errors" should {
      "return the errors" in {
        val requestHandler = RequestHandler
          .withValidator(singleErrorValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header(header = "X-CorrelationId", of = result) shouldBe Some(generatedCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "a request fails with service errors" should {
      "return the errors" in {
        val requestHandler = successRequestHandler.withPlainJsonResult(successCode)

        service.returns(Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError))))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header(header = "X-CorrelationId", of = result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "given an error handler that doesn't handle an error" should {
      "return an InternalServerError" in {
        service.returns(Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError))))

        val errorHandler = ErrorHandling {
          case _: ErrorWrapper if false =>
            throw new Exception("Should not have been matched")
        }

        val requestHandler = successRequestHandler.withErrorHandling(errorHandler)

        val result = requestHandler.handleRequest()
        status(result) shouldBe InternalError.httpStatus
        contentAsJson(result) shouldBe InternalError.asJson
      }
    }

  }

  "withErrorHandling()" should {
    "return a new RequestHandlerBuilder with the expected error handling" in {
      class CustomErrorHandling extends ErrorHandling(null)

      val result = successRequestHandler.withErrorHandling(new CustomErrorHandling)
      result.errorHandling shouldBe a[CustomErrorHandling]
    }
  }

}

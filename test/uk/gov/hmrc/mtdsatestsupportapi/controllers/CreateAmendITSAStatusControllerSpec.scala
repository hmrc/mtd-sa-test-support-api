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
import api.mocks.MockIdGenerator
import api.mocks.services.MockAuthService
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, InternalError, NinoFormatError, ServiceUnavailableError}
import api.models.outcomes.ResponseWrapper
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.mtdsatestsupportapi.mocks.requestParsers.MockCreateAmendITSAStatusRequestParser
import uk.gov.hmrc.mtdsatestsupportapi.mocks.services.MockCreateAmendITSAStatusService
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{
  CreateAmendITSAStatusRawData,
  CreateAmendITSAStatusRequest,
  CreateAmendITSAStatusRequestBody,
  ITSAStatusDetail
}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CreateAmendITSAStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerSpecHateoasSupport
    with ControllerTestRunner
    with MockCreateAmendITSAStatusService
    with MockCreateAmendITSAStatusRequestParser
    with MockAuthService
    with MockIdGenerator {

  trait Test extends ControllerTest {
    protected val vendorClientId = "some_id"

    val taxYear: TaxYear = TaxYear.fromMtd("2019-20")

    // move to fixture?
    val body: JsValue = Json.parse("""
                                     |{
                                     |  "itsaStatusDetails": [
                                     |    {
                                     |      "submittedOn": "2021-03-23T16:02:34.039Z",
                                     |      "status": "01",
                                     |      "statusReason": "02",
                                     |      "businessIncome2YearsPrior": 234
                                     |    }
                                     |  ]
                                     |}
                                     |""".stripMargin)

    val parsedBody = CreateAmendITSAStatusRequestBody(itsaStatusDetails = Seq(
      ITSAStatusDetail(
        submittedOn = "2021-03-23T16:02:34.039Z",
        status = Status.`01`,
        statusReason = StatusReason.`02`,
        businessIncome2YearsPrior = Some(234))))

    val rawData: CreateAmendITSAStatusRawData     = CreateAmendITSAStatusRawData(nino, taxYear.asMtd, body)
    val requestData: CreateAmendITSAStatusRequest = CreateAmendITSAStatusRequest(Nino(nino), taxYear, parsedBody)

    val controller = new CreateAmendITSAStatusController(
      cc = cc,
      authService = mockEnrolmentsAuthService,
      parser = mockRequestParser,
      service = mockService,
      idGenerator = mockIdGenerator)

    protected val hateoasResponse: JsObject = hateoaslinksJson

  }

  "handleRequest" should {
    "return NO_CONTENT" when {
      "a valid request is processed successfully" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, taxYear.asMtd)(
            fakeRequest.withBody(body).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateAmendITSAStatusRequestParser.parseRequest(rawData).returns(Right(requestData))

        MockCreateAmendITSAStatusService.createAmend(requestData).returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, taxYear.asMtd)(
            fakeRequest.withBody(body).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateAmendITSAStatusRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)

      }

      "the service returns an error" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, taxYear.asMtd)(
            fakeRequest.withBody(body).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token", "X-Client-Id" -> "some_id"))

        MockCreateAmendITSAStatusRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateAmendITSAStatusService
          .createAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, ServiceUnavailableError))))

        runErrorTest(ServiceUnavailableError)
      }
    }

    "return an InternalServerError" when {
      "the request is missing an X-Client-Id header" in new Test {
        override protected def callController(): Future[Result] =
          controller.handleRequest(nino, taxYear.asMtd)(fakeRequest.withBody(body).withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token"))

        val result: Future[Result] = callController()

        status(result) shouldBe InternalError.httpStatus
      }
    }
  }

}

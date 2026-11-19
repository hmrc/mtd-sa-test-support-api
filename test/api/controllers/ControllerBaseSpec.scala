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

import api.config.MockAppConfig
import api.models.errors.MtdError
import api.services.MockAuthService
import api.utils.{MockIdGenerator, UnitSpec}
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ControllerBaseSpec extends UnitSpec with Status with MimeTypes with HeaderNames with ResultExtractors with MockAppConfig {

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val cc: ControllerComponents = stubControllerComponents()

  lazy val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
    .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token")
    .withMethod("DELETE")

  lazy val fakePostEmptyRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
    .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer Token")
    .withMethod("POST")

  def fakePostRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  def fakeRequestWithHeaders(headers: (String, String)*): FakeRequest[AnyContentAsEmpty.type] =
    fakeRequest.withHeaders(headers*)

  def fakeRequestWithBody[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  def withPath(error: MtdError): MtdError = error.copy(paths = Some(Seq("/somePath")))

}

trait ControllerTestRunner extends ControllerBaseSpec with MockAuthService with MockIdGenerator {
  protected val nino: String  = "AA123456A"
  protected val correlationId = "X-123"

  trait ControllerTest {
    protected val hc: HeaderCarrier = HeaderCarrier()

    MockedEnrolmentsAuthService.authoriseUser()
    MockedIdGenerator.getCorrelationId.returns(correlationId)
    MockedAppConfig.apiGatewayContext.returns("individuals/vendor-state/checkpoints").anyNumberOfTimes()

    def runOkTest(expectedStatus: Int, maybeExpectedResponseBody: Option[JsValue] = None): Unit = {
      val result: Future[Result] = callController()

      status(result) shouldBe expectedStatus
      header(header = "X-CorrelationId", of = result) shouldBe Some(correlationId)

      maybeExpectedResponseBody match {
        case Some(jsBody) => contentAsJson(result) shouldBe jsBody
        case None         => contentType(result) shouldBe empty
      }
    }

    protected def runErrorTest(expectedError: MtdError): Unit = {
      val result: Future[Result] = callController()

      status(result) shouldBe expectedError.httpStatus
      header(header = "X-CorrelationId", of = result) shouldBe Some(correlationId)

      contentAsJson(result) shouldBe expectedError.asJson
    }

    protected def runStatusCheck(expectedStatus: Int): Unit = {
      val result: Future[Result] = callController()

      status(result) shouldBe expectedStatus
    }

    protected def callController(): Future[Result]
  }

}

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

package api.connectors

import mocks.{MockAppConfig, MockHttpClient}
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import utils.UrlUtils

import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl                   = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  protected val notPassedThroughHeader: (String, String) = "NotPassedThroughHeader" -> "NotPassedThroughValue"
  protected val passedThroughHeader: (String, String) = "PassedThroughHeader"    -> "PassedThroughValue"

  val otherHeaders: Seq[(String, String)] = Seq(passedThroughHeader, notPassedThroughHeader)

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("this-api")
    )

  val requiredStubHeaders: Seq[(String, String)] = Seq(
    "CorrelationId" -> correlationId,
    passedThroughHeader
  )

  val allowedStubHeaders: Seq[String] = Seq(
    "Content-Type",
    "PassedThroughHeader"
  )

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]

    protected def willGet[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockHttpClient
        .get(
          url = url,
          parameters = parameters,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq(notPassedThroughHeader)
        )
    }

    protected def willPost[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .post(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq(notPassedThroughHeader)
        )
    }

    protected def willPut[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq(notPassedThroughHeader)
        )
    }

    protected def willDelete[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      val fullUrl = UrlUtils.appendQueryParams(url, parameters)

      MockHttpClient
        .delete(
          url = fullUrl,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq(notPassedThroughHeader)
        )
    }

  }

  protected trait StubTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredStubHeaders

    MockAppConfig.stubBaseUrl returns baseUrl
    MockAppConfig.stubEnvironmentHeaders returns Some(allowedStubHeaders)

  }

}

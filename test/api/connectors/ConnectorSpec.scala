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

package api.connectors

import api.config.MockAppConfig
import api.mocks.MockHttpClient
import api.utils.UnitSpec
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl: String           = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val otherHeaders: Seq[(String, String)] = List(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      List("^not-test-BaseUrl?$".r),
      Nil,
      Some("mtd-sa-test-support-api")
    )

  val allowedHeaders: Seq[String] = List(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  protected trait StubTest extends MockHttpClient with MockAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)] = List(
      "Authorization"     -> "Bearer stub-token",
      "Environment"       -> "stub-environment",
      "User-Agent"        -> "mtd-sa-test-support-api",
      "CorrelationId"     -> correlationId,
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    MockedAppConfig.stubBaseUrl returns baseUrl
    MockedAppConfig.stubToken returns "stub-token"
    MockedAppConfig.stubEnv returns "stub-environment"
    MockedAppConfig.stubEnvironmentHeaders returns Some(allowedHeaders)

    protected def willGet[T](url: URL, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockedHttpClient.get(
        url = url,
        config = dummyHeaderCarrierConfig,
        parameters = parameters,
        requiredHeaders = requiredHeaders,
        excludedHeaders = List("AnotherHeader" -> "HeaderValue")
      )
    }

    protected def willPost[BODY, T](url: URL, body: BODY)(implicit writes: Writes[BODY]): CallHandler[Future[T]] = {
      MockedHttpClient.post(
        url = url,
        config = dummyHeaderCarrierConfig,
        body = Json.toJson(body),
        requiredHeaders = requiredHeaders ++ List("Content-Type" -> "application/json"),
        excludedHeaders = List("AnotherHeader" -> "HeaderValue")
      )
    }

    protected def willPostEmpty[T](url: URL): CallHandler[Future[T]] = {
      MockedHttpClient.postEmpty(
        url = url,
        config = dummyHeaderCarrierConfig,
        requiredHeaders = requiredHeaders,
        excludedHeaders = List("AnotherHeader" -> "HeaderValue")
      )
    }

    protected def willPut[BODY, T](url: URL, body: BODY)(implicit writes: Writes[BODY]): CallHandler[Future[T]] = {
      MockedHttpClient.put(
        url = url,
        config = dummyHeaderCarrierConfig,
        body = Json.toJson(body),
        requiredHeaders = requiredHeaders ++ List("Content-Type" -> "application/json"),
        excludedHeaders = List("AnotherHeader" -> "HeaderValue")
      )
    }

    protected def willDelete[T](url: URL, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockedHttpClient.delete(
        url = url,
        config = dummyHeaderCarrierConfig,
        parameters = parameters,
        requiredHeaders = requiredHeaders,
        excludedHeaders = List("AnotherHeader" -> "HeaderValue")
      )
    }

  }

}

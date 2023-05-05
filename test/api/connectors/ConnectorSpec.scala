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
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import utils.UrlUtils

import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl                   = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("this-api")
    )

  val requiredDesHeaders: Seq[(String, String)] = Seq(
    "Authorization"     -> "Bearer des-token",
    "Environment"       -> "des-environment",
    "User-Agent"        -> "this-api",
    "CorrelationId"     -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val allowedDesHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  val requiredIfsHeaders: Seq[(String, String)] = Seq(
    "Authorization"     -> "Bearer ifs-token",
    "Environment"       -> "ifs-environment",
    "User-Agent"        -> "this-api",
    "CorrelationId"     -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val allowedIfsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  val requiredTysIfsHeaders: Seq[(String, String)] = Seq(
    "Authorization"     -> "Bearer TYS-IFS-token",
    "Environment"       -> "TYS-IFS-environment",
    "User-Agent"        -> "this-api",
    "CorrelationId"     -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val allowedTysIfsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
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
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willPost[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .post(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willPut[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willDelete[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      val fullUrl = UrlUtils.appendQueryParams(url, parameters)

      MockHttpClient
        .delete(
          url = fullUrl,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

  }

  protected trait DesTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredDesHeaders

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    MockAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)

  }

  protected trait IfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredIfsHeaders

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)

    MockAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)
  }

  protected trait TysIfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredTysIfsHeaders

    MockAppConfig.tysIfsBaseUrl returns baseUrl
    MockAppConfig.tysIfsToken returns "TYS-IFS-token"
    MockAppConfig.tysIfsEnvironment returns "TYS-IFS-environment"
    MockAppConfig.tysIfsEnvironmentHeaders returns Some(allowedIfsHeaders)

    MockAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> true)
  }

}

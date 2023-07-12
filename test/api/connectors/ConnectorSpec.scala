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

import config.DownstreamConfig
import mocks.{MockAppConfig, MockHttpClient}
import stubs.DownstreamStub
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.ExecutionContext

trait ConnectorSpec extends UnitSpec with WireMockSupport with DownstreamStub with HttpClientV2Support {
  implicit val requestCorrelationId: String = "requestCorrelationId"
  val responseCorrelationId: String         = "responseCorrelationId"

  protected val baseUrl: String = s"http://localhost:$wireMockPort"

  protected val notPassedThroughHeader: (String, String) = "NotPassedThroughHeader" -> "NotPassedThroughValue"
  protected val passedThroughHeader: (String, String)    = "PassedThroughHeader"    -> "PassedThroughValue"

  val otherHeaders: Seq[(String, String)] = Seq(passedThroughHeader, notPassedThroughHeader)

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val responseHeaders: Seq[(String, String)] = Seq("correlationId" -> responseCorrelationId)

  val requiredHeaders: Seq[(String, String)] = Seq(
    "CorrelationId" -> requestCorrelationId,
    passedThroughHeader
  )

  val allowedHeaderNames: Seq[String] = Seq(
    "Content-Type",
    "PassedThroughHeader"
  )

  val downstreamConfig: DownstreamConfig = DownstreamConfig(baseUrl, Some(allowedHeaderNames))

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig

  protected trait StubTest extends ConnectorTest {
    MockAppConfig.stubDownstreamConfig returns downstreamConfig
  }

}

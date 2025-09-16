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

package api.connectors

import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.models.outcomes.ResponseWrapper
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.{MatchResult, ValueMatcher}
import config.AppConfig
import mocks.MockAppConfig
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

class BaseDownstreamConnectorSpec extends ConnectorSpec {
  case class RequestBody(requestValue: String)

  object RequestBody {
    implicit val writes: OWrites[RequestBody] = Json.writes
  }

  case class ResponseBody(responseValue: String)

  object ResponseBody {
    implicit val reads: Reads[ResponseBody] = Json.reads
  }

  val requestBody: RequestBody  = RequestBody("request value")
  val requestBodyJson: JsObject = Json.obj("requestValue" -> "request value")

  val responseBody: ResponseBody = ResponseBody("response value")
  val responseBodyJson: JsObject = Json.obj("responseValue" -> "response value")

  val outcome: Right[Nothing, ResponseWrapper[ResponseBody]] = Right(ResponseWrapper(responseCorrelationId, responseBody))
  val downstreamUri: DownstreamUri[ResponseBody]             = DownstreamUri[ResponseBody](s"some/path")
  val url                                                    = "/some/path"

  class Test extends MockAppConfig with StandardDownstreamHttpParser {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClientV2   = httpClientV2
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.stubDownstreamConfig returns downstreamConfig
    MockAppConfig.stubEnv returns stubEnv
    MockAppConfig.stubToken returns stubToken

    def excludedRequestHeadersNames(excludedHeaderNames: String*): ValueMatcher[Request] = { (request: Request) =>
      val containsHeader = excludedHeaderNames.exists(headerName => request.containsHeader(headerName))
      MatchResult.of(!containsHeader)
    }

    def singleValuedHeader(name: String, value: String): ValueMatcher[Request] = { (request: Request) =>
      val header = request.getHeaders.getHeader(name)

      MatchResult.of(header.values().size() == 1 && header.values().get(0) == value)
    }

  }

  "for the stub" when {
    "post" must {
      "posts with the required headers and returns the result" in new Test {
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest :+ ("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        val requiredStubHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        when(POST, url)
          .withHeaders(requiredStubHeadersPost)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .withRequestBody(requestBodyJson)
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.post(requestBody, downstreamUri)) shouldBe outcome
      }
    }

    "post empty body" must {
      "posts with the required headers and returns the result" in new Test {
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest :+ ("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        when(POST, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.postEmpty(downstreamUri)) shouldBe outcome
      }
    }

    "get" must {
      "get with the required headers and return the result" in new Test {
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest :+ ("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        when(GET, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.get(downstreamUri)) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required headers and return the result" in new Test {
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest :+ ("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        when(DELETE, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.delete(downstreamUri)) shouldBe outcome
      }
    }

    "put" must {
      "put with the required headers and return result" in new Test {
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest ++ Seq("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        val requiredStubHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        when(PUT, url)
          .withHeaders(requiredStubHeadersPut)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .withRequestBody(requestBodyJson)
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.put(requestBody, downstreamUri)) shouldBe outcome
      }
    }

    "put empty body" must {
      "puts with the required headers and returns the result" in new Test {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = headersInRequest :+ ("Content-Type" -> "application/json"))
        implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

        when(PUT, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.putEmpty(downstreamUri)) shouldBe outcome
      }
    }

    "content-type header already present and set to be passed through" must {
      "override (not duplicate) the value" when {
        testNoDuplicatedContentType("Content-Type" -> "application/user-type")
        testNoDuplicatedContentType("content-type" -> "application/user-type")

        def testNoDuplicatedContentType(userContentType: (String, String)): Unit =
          s"for user content type header $userContentType" in new Test {
            implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = headersInRequest :+ userContentType)
            implicit val context: connector.ConnectorContext = connector.ConnectorContext(downstreamConfig)

            when(PUT, url)
              .withHeaders(requiredHeaders ++ Seq("Content-Type" -> "application/json"))
              .withCustomMatcher(singleValuedHeader("Content-Type", "application/json"))
              .withRequestBody(requestBody)
              .thenReturn(OK, responseBodyJson, responseHeaders)

            await(connector.put(requestBody, downstreamUri)) shouldBe outcome
          }
      }
    }
  }

}

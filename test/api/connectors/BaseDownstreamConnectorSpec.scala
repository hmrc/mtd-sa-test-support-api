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

import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.models.outcomes.ResponseWrapper
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.{MatchResult, ValueMatcher}
import config.AppConfig
import mocks.{MockAppConfig, MockHttpClient}
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

class BaseDownstreamConnectorSpec extends WiremockConnectorSpec {
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
  val downstreamUri: DownstreamUri[ResponseBody]             = DownstreamUri[ResponseBody](url"$baseUrl/some/path")
  val url                                                    = "/some/path"

  class Test extends MockHttpClient with MockAppConfig with StandardDownstreamHttpParser {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClientV2   = httpClientV2
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.stubDownstreamConfig returns downstreamConfig

    def excludedRequestHeadersNames(excludedHeaderNames: String*): ValueMatcher[Request] = { request: Request =>
      val containsHeader = excludedHeaderNames.exists(headerName => request.containsHeader(headerName))
      MatchResult.of(!containsHeader)
    }

    def singleValuedHeader(name: String, value: String): ValueMatcher[Request] = { request: Request =>
      val header = request.getHeaders.getHeader(name)

      MatchResult.of(header.values().size() == 1 && header.values().get(0) == value)
    }

  }

  "for the stub" when {
    "post" must {
      "posts with the required headers and returns the result" in new Test {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders.toSeq :+ ("Content-Type" -> "application/json"))

        val requiredStubHeadersPost: Map[String, String] = requiredHeaders + ("Content-Type" -> "application/json")

        when(POST, url)
          .withHeaders(requiredStubHeadersPost)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .withRequestBody(requestBodyJson)
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.post(downstreamConfig)(requestBody, downstreamUri)) shouldBe outcome
      }
    }

    "get" must {
      "get with the required headers and return the result" in new Test {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders.toSeq :+ ("Content-Type" -> "application/json"))

        when(GET, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.get(downstreamConfig)(downstreamUri)) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required headers and return the result" in new Test {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders.toSeq :+ ("Content-Type" -> "application/json"))

        when(DELETE, url)
          .withHeaders(requiredHeaders)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.delete(downstreamConfig)(downstreamUri)) shouldBe outcome
      }
    }

    "put" must {
      "put with the required headers and return result" in new Test {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders.toSeq ++ Seq("Content-Type" -> "application/json"))

        val requiredStubHeadersPut: Map[String, String] = requiredHeaders + ("Content-Type" -> "application/json")

        when(PUT, url)
          .withHeaders(requiredStubHeadersPut)
          .withCustomMatcher(excludedRequestHeadersNames("NotPassedThroughHeader"))
          .withRequestBody(requestBodyJson)
          .thenReturn(OK, responseBodyJson, responseHeaders)

        await(connector.put(downstreamConfig)(requestBody, downstreamUri)) shouldBe outcome
      }
    }

    "content-type header already present and set to be passed through" must {
      "override (not duplicate) the value" when {
        testNoDuplicatedContentType("Content-Type" -> "application/user-type")
        testNoDuplicatedContentType("content-type" -> "application/user-type")

        def testNoDuplicatedContentType(userContentType: (String, String)): Unit =
          s"for user content type header $userContentType" in new Test {
            implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders.toSeq :+ userContentType)

            when(PUT, url)
              .withHeaders(requiredHeaders + ("Content-Type" -> "application/json"))
              .withCustomMatcher(singleValuedHeader("Content-Type", "application/json"))
              .withRequestBody(requestBody)
              .thenReturn(OK, responseBodyJson, responseHeaders)

            await(connector.put(downstreamConfig)(requestBody, downstreamUri)) shouldBe outcome
          }
      }
    }
  }

}

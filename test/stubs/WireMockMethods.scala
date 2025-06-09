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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder => WMappingBuilder}
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.{UrlPattern, ValueMatcher}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Writes
import uk.gov.hmrc.http.test.WireMockSupport

trait WireMockMethods {
  _: WireMockSupport =>

  def when(method: HTTPMethod,
           uri: String,
           queryParams: Seq[(String, String)] = Seq.empty,
           headers: Seq[(String, String)] = Seq.empty): MappingBuilder =
    when(method, uri).withQueryParams(queryParams).withHeaders(headers)

  def when(method: HTTPMethod, uri: String): MappingBuilder =
    new MappingBuilder(method.wireMockMapping(urlPathMatching(uri)))

  // Wrapper for Wiremock's own MappingBuilder
  class MappingBuilder(mappingBuilder: WMappingBuilder) {

    def withQueryParams(queryParams: Seq[(String, String)]): MappingBuilder = {
      queryParams.foreach { case (key, value) => mappingBuilder.withQueryParam(key, matching(value)) }
      this
    }

    def withHeaders(headers: Seq[(String, String)]): MappingBuilder = {
      headers.foreach { case (key, value) => mappingBuilder.withHeader(key, equalTo(value)) }
      this
    }

    def withCustomMatcher(customMatcher: ValueMatcher[Request]): MappingBuilder = {
      mappingBuilder.andMatching(customMatcher)
      this
    }

    def withRequestBody(body: String): MappingBuilder = {
      mappingBuilder.withRequestBody(equalTo(body))
      this
    }

    def withRequestBody[T](body: T)(implicit writes: Writes[T]): MappingBuilder = {
      val stringBody = writes.writes(body).toString()
      mappingBuilder.withRequestBody(equalToJson(stringBody))
      this
    }

    def thenReturn[T](status: Int, body: T, headers: Seq[(String, String)] = Seq.empty)(implicit writes: Writes[T]): StubMapping = {
      val stringBody = writes.writes(body).toString()
      thenReturnInternal(status, headers, Some(stringBody))
    }

    def thenReturnNoContent(status: Int = 204, headers: Seq[(String, String)] = Seq.empty): StubMapping = {
      thenReturnInternal(status, headers, None)
    }

    private def thenReturnInternal(status: Int, headers: Seq[(String, String)], body: Option[String]): StubMapping = {
      val response = {
        val statusResponse = aResponse().withStatus(status)
        val responseWithHeaders = headers.foldLeft(statusResponse) { case (res, (key, value)) =>
          res.withHeader(key, value)
        }
        body match {
          case Some(extractedBody) => responseWithHeaders.withBody(extractedBody)
          case None                => responseWithHeaders
        }
      }

      wireMockServer.stubFor(mappingBuilder.willReturn(response))
    }

  }

  sealed trait HTTPMethod {
    def wireMockMapping(pattern: UrlPattern): WMappingBuilder
  }

  case object POST extends HTTPMethod {
    override def wireMockMapping(pattern: UrlPattern): WMappingBuilder = post(pattern)
  }

  case object GET extends HTTPMethod {
    override def wireMockMapping(pattern: UrlPattern): WMappingBuilder = get(pattern)
  }

  case object DELETE extends HTTPMethod {
    override def wireMockMapping(pattern: UrlPattern): WMappingBuilder = delete(pattern)
  }

  case object PUT extends HTTPMethod {
    override def wireMockMapping(pattern: UrlPattern): WMappingBuilder = put(pattern)
  }

}

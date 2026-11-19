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

package api.mocks

import api.utils.UrlUtils
import izumi.reflect.Tag
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.JsValue
import play.api.libs.ws.BodyWritable
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends TestSuite with MockFactory {

  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  object MockedHttpClient extends Matchers {

    def get[T](url: URL,
               config: HeaderCarrier.Config,
               parameters: Seq[(String, String)] = Seq.empty,
               requiredHeaders: Seq[(String, String)] = Seq.empty,
               excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .get(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
          {
            val expectedUrl = UrlUtils.appendQueryParams(url.toString, parameters)
            actualUrl.toString shouldBe expectedUrl

            val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
        .returns(mockRequestBuilder)
      (mockRequestBuilder.execute[T](using _: HttpReads[T], _: ExecutionContext)).expects(*, *)
    }

    def post[T](url: URL,
                config: HeaderCarrier.Config,
                body: JsValue,
                requiredHeaders: Seq[(String, String)] = Seq.empty,
                excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .post(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
          {
            actualUrl shouldBe url

            val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
        .returns(mockRequestBuilder)

      (mockRequestBuilder
        .withBody(_: JsValue)(using _: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext))
        .expects(body, *, *, *)
        .returns(mockRequestBuilder)
      (mockRequestBuilder
        .execute[T](using _: HttpReads[T], _: ExecutionContext))
        .expects(*, *)
    }

    def postEmpty[T](url: URL,
                     config: HeaderCarrier.Config,
                     requiredHeaders: Seq[(String, String)] = Seq.empty,
                     excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {

      (mockHttpClient
        .post(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
          actualUrl shouldBe url

          val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
          assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
        })
        .returns(mockRequestBuilder)

      (mockRequestBuilder.execute(using _: HttpReads[T], _: ExecutionContext)).expects(*, *)
    }

    def put[T](url: URL,
               config: HeaderCarrier.Config,
               body: JsValue,
               requiredHeaders: Seq[(String, String)] = Seq.empty,
               excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .put(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
          {
            actualUrl shouldBe url

            val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
        .returns(mockRequestBuilder)

      (mockRequestBuilder
        .withBody(_: JsValue)(using _: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext))
        .expects(body, *, *, *)
        .returns(mockRequestBuilder)
      (mockRequestBuilder
        .execute[T](using _: HttpReads[T], _: ExecutionContext))
        .expects(*, *)
    }

    def delete[T](url: URL,
                  config: HeaderCarrier.Config,
                  parameters: Seq[(String, String)] = Seq.empty,
                  requiredHeaders: Seq[(String, String)] = Seq.empty,
                  excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .delete(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
          {
            val expectedUrl = UrlUtils.appendQueryParams(url.toString, parameters)
            actualUrl.toString shouldBe expectedUrl

            val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
        .returns(mockRequestBuilder)
      (mockRequestBuilder.execute[T](using _: HttpReads[T], _: ExecutionContext)).expects(*, *)
    }

    private def assertHeaders(actualHeaders: Seq[(String, String)],
                              requiredHeaders: Seq[(String, String)],
                              excludedHeaders: Seq[(String, String)]) = {

      actualHeaders should contain allElementsOf requiredHeaders
      actualHeaders should contain noElementsOf excludedHeaders
    }

  }

}

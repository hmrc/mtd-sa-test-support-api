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

package mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient, HttpReads }

import scala.concurrent.{ ExecutionContext, Future }

trait MockHttpClient extends MockFactory {

  val mockHttpClient: HttpClient = mock[HttpClient]

  object MockHttpClient extends Matchers {

    def get[T](url: String,
               config: HeaderCarrier.Config,
               parameters: Seq[(String, String)] = Nil,
               requiredHeaders: Seq[(String, String)] = Nil,
               excludedHeaders: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String, _: Seq[(String, String)], _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs {
          (actualUrl: String,
           actualParams: Seq[(String, String)],
           _: Seq[(String, String)],
           _: HttpReads[T],
           hc: HeaderCarrier,
           _: ExecutionContext) =>
            {
              actualUrl shouldBe url
              actualParams should contain theSameElementsAs parameters

              val headersForUrl = hc.headersForUrl(config)(actualUrl)
              assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
            }
        })
    }

    def post[I, T](url: String,
                   config: HeaderCarrier.Config,
                   body: I,
                   requiredHeaders: Seq[(String, String)] = Nil,
                   excludedHeaders: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      (mockHttpClient
        .POST[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url
            actualBody shouldBe body

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    def put[I, T](url: String,
                  config: HeaderCarrier.Config,
                  body: I,
                  requiredHeaders: Seq[(String, String)] = Nil,
                  excludedHeaders: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      (mockHttpClient
        .PUT[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url
            actualBody shouldBe body

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    def delete[T](url: String,
                  config: HeaderCarrier.Config,
                  requiredHeaders: Seq[(String, String)] = Nil,
                  excludedHeaders: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      (mockHttpClient
        .DELETE(_: String, _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    private def assertHeaders[T, I](actualHeaders: Seq[(String, String)],
                                    requiredHeaders: Seq[(String, String)],
                                    excludedHeaders: Seq[(String, String)]) = {

      actualHeaders should contain allElementsOf requiredHeaders
      actualHeaders should contain noElementsOf excludedHeaders
    }

  }

}

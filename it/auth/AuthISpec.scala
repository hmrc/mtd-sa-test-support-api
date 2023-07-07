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

package auth

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    private val vendorClientId = "some_id"
    private val mtdUri         = "/vendor-state"
    val stubUri                = s"/test-support/vendor-state/$vendorClientId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"), // some bearer token
          ("X-Client-Id", vendorClientId)
        )
    }

  }

  "Invoking the delete vendor state endpoint" when {
    "the user is authorised" should {
      "return a 204 response" in new Test {
        override def setupStubs(): StubMapping = {
          authorised()
          onSuccess(DELETE, stubUri, Map.empty, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
      }
    }

    "the user is not logged in" should {
      "return 403" in new Test {
        override def setupStubs(): StubMapping = unauthorisedNotLoggedIn()

        val response: WSResponse = await(request().delete())
        response.status shouldBe UNAUTHORIZED
      }
    }

    "the user is not authorised" should {
      "return 403" in new Test {
        override def setupStubs(): StubMapping = unauthorisedOther()

        val response: WSResponse = await(request().delete())
        response.status shouldBe UNAUTHORIZED
      }
    }
  }

}

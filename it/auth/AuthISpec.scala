package auth

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import stubs.{AuthStub, DownstreamStub}
import support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val vendorClientId = "some_id"
    val stubUri        = s"/test-support/vendor-state/$vendorClientId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(stubUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Invoking the delete vendor state endpoint" when {
    "the user is authorised" should {
      "return a 204 response" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.DELETE, stubUri, Map.empty, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
      }
    }

    "the user is not logged in" should {
      "return 403" in new Test {
        override def setupStubs(): StubMapping = AuthStub.unauthorisedNotLoggedIn()

        val response: WSResponse = await(request().delete())
        response.status shouldBe FORBIDDEN
      }
    }

    "the user is not authorised" should {
      "return 403" in new Test {
        override def setupStubs(): StubMapping = AuthStub.unauthorisedOther()

        val response: WSResponse = await(request().delete())
        response.status shouldBe FORBIDDEN
      }
    }
  }

}

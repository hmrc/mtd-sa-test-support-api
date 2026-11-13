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

package v1.listCheckpoints

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v1.listCheckpoints.fixture.ListCheckpointsFixture.{downstreamResponseBodyJson, mtdResponseBodyJson}

class ListCheckpointsControllerISpec extends IntegrationBaseSpec {

  "Calling the 'List Checkpoints' endpoint" should {
    "return a 200 status code" when {
      "a valid request is made with query parameter nino" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = GET,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = OK,
          body = downstreamResponseBodyJson
        )

        val response: WSResponse = await(request().get())

        response.status shouldBe OK
        response.json shouldBe mtdResponseBodyJson
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "a valid request is made without query parameter nino" in new Test {
        override val mtdQueryParams: Seq[(String, String)]        = Seq.empty
        override val downstreamQueryParams: Seq[(String, String)] = Seq.empty

        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = GET,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = OK,
          body = downstreamResponseBodyJson
        )

        val response: WSResponse = await(request().get())

        response.status shouldBe OK
        response.json shouldBe mtdResponseBodyJson
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation fails with FORMAT_NINO error" in new Test {
        override val mtdQueryParams: Seq[(String, String)] = Seq("nino" -> "AA1123A")

        val response: WSResponse = await(request().get())

        response.status shouldBe BAD_REQUEST
        response.json shouldBe NinoFormatError.asJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "stub returns an unmapped error code and status 500" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onError(
          method = GET,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          errorStatus = INTERNAL_SERVER_ERROR,
          errorBody = downstreamErrorBody("UNMAPPED")
        )

        val response: WSResponse = await(request().get())

        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe InternalError.asJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }

  private trait Test {
    private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
    private val nino: String           = "AA123456A"

    val mtdQueryParams: Seq[(String, String)] = Seq("nino" -> nino)

    val downstreamUri: String                        = s"/test-support/vendor-state/$vendorClientId/checkpoints"
    val downstreamQueryParams: Seq[(String, String)] = Seq("taxableEntityId" -> nino)

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest("/vendor-state/checkpoints")
        .addQueryStringParameters(mtdQueryParams*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

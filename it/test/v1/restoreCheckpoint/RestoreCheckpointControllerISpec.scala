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

package v1.restoreCheckpoint

import api.models.errors.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*

class RestoreCheckpointControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Restore to Checkpoint' endpoint" should {
    "return a 201 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = POST,
          uri = downstreamUri,
          status = CREATED,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().post(JsNull))

        response.status shouldBe CREATED
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation fails with FORMAT_CHECKPOINT_ID error" in new Test {
        override val checkpointId: String = "a1e8057e"

        val response: WSResponse = await(request().post(JsNull))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe CheckpointIdFormatError.asJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "stub returns a code NOT_FOUND error and status 404" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onError(
          method = POST,
          uri = downstreamUri,
          errorStatus = NOT_FOUND,
          errorBody = downstreamErrorBody("NOT_FOUND")
        )

        val response: WSResponse = await(request().post(JsNull))

        response.status shouldBe NOT_FOUND
        response.json shouldBe NotFoundError.asJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }

  private trait Test {
    private val vendorClientId: String = "Abc1Def2Ghi3Jkl4Mno5Pqr6Stu7"
    val checkpointId: String           = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

    val downstreamUri: String = s"/test-support/vendor-state/$vendorClientId/checkpoints/$checkpointId/restore"

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      setupStubs()
      buildRequest(s"/vendor-state/checkpoints/$checkpointId/restore")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-Client-Id", vendorClientId)
        )
    }

  }

}

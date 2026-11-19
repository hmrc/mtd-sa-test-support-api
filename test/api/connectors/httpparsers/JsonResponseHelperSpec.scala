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

package api.connectors.httpparsers

import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class JsonResponseHelperSpec extends UnitSpec with HttpParser with LogCapturing {

  private case class SomeModel(data: String)

  private object SomeModel {
    implicit val reads: Reads[SomeModel] = Json.reads[SomeModel]
  }

  private val data: String     = "someData"
  private val model: SomeModel = SomeModel(data)

  private val headers: Map[String, Seq[String]] = Map("CorrelationId" -> Seq("a1e8057e-fbbc-47a8-a8b4-78d9f015c253"))
  private val validJsonResponse: HttpResponse   = HttpResponse(OK, Json.obj("data" -> data), headers)
  private val invalidJsonResponse: HttpResponse = HttpResponse(OK, Json.obj("data" -> 1234), headers)
  private val nonJsonResponse: HttpResponse     = HttpResponse(OK, "", headers)

  "JsonResponseHelper" when {
    "validateJson" when {
      "the response body contains valid JSON matching the expected model" should {
        "return the parsed model" in {
          validJsonResponse.validateJson[SomeModel] shouldBe Some(model)
        }
      }

      "the response body contains valid JSON that does not match the expected model" should {
        "return None and not log" in {
          withCaptureOfLoggingFrom(logger) { events =>
            invalidJsonResponse.validateJson[SomeModel] shouldBe None

            events shouldBe empty
          }
        }
      }

      "the response body is not valid JSON" should {
        "return None and not log" in {
          withCaptureOfLoggingFrom(logger) { events =>
            nonJsonResponse.validateJson[SomeModel] shouldBe None

            events shouldBe empty
          }
        }
      }
    }

    "validateJsonWithLogging" when {
      "the response body contains valid JSON matching the expected model" should {
        "return the parsed model" in {
          validJsonResponse.validateJsonWithLogging[SomeModel] shouldBe Some(model)
        }
      }

      "the response body contains valid JSON that does not match the expected model" should {
        "return None and log the validation failure" in {
          withCaptureOfLoggingFrom(logger) { events =>
            invalidJsonResponse.validateJsonWithLogging[SomeModel] shouldBe None

            events.head.getMessage should startWith("[JsonResponseHelper][validateJsonWithLogging] JSON validation failed:")
          }
        }
      }

      "the response body is not valid JSON" should {
        "return None and log that the response body is not valid JSON" in {
          withCaptureOfLoggingFrom(logger) { events =>
            nonJsonResponse.validateJsonWithLogging[SomeModel] shouldBe None

            events.head.getMessage shouldBe "[JsonResponseHelper][validateJsonWithLogging] Response body is not valid JSON"
          }
        }
      }
    }
  }

}

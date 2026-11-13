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

package config

import play.api.http.Status
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_String
import support.IntegrationBaseSpec

class InternalAuthFeatureSwitchISpec extends IntegrationBaseSpec {

  "Internal authorization with the default configuration" should {
    Seq("/auth/generate", "/auth/generate/").foreach { path =>
      s"return 404 for $path before parsing the request or invoking services" in {
        val response = await(buildRequest(path).withHttpHeaders("Content-Type" -> "application/json").post("invalid json"))

        response.status shouldBe Status.NOT_FOUND
      }
    }

    "omit the endpoint from the published OAS" in {
      val response = await(buildRequest("/api/conf/1.0/application.yaml").get())

      response.status shouldBe Status.OK
      response.body should not include "/auth/generate"
      response.body should not include "auth_generate.yaml"
      response.body should include("/individuals/self-assessment-test-support/vendor-state")
    }

    "omit its error examples from shared documentation" in {
      val response = await(buildRequest("/api/conf/1.0/common/errors.yaml").get())

      response.status shouldBe Status.OK
      response.body should not include "notFoundMtdId"
      response.body should not include "GRANTSCOPE_NOT_FOUND"
      response.body should not include "OAUTH_CODE_RETRIEVAL_ERROR"
      response.body should not include "authInternalError"
    }

    Seq(
      "auth_generate.yaml",
      "schemas/auth/generate_request.json",
      "schemas/auth/generate_response.json",
      "schemas/auth/validation_error_response.json",
      "examples/auth/generate_request_lookup.json",
      "examples/auth/generate_request_identifier.json",
      "examples/auth/generate_response.json",
      "examples/auth/generate_validation_error_response.json"
    ).foreach { filename =>
      s"return 404 for a direct request for $filename" in {
        await(buildRequest(s"/api/conf/1.0/$filename").get()).status shouldBe Status.NOT_FOUND
      }
    }
  }

}

/*
 * Copyright 2025 HM Revenue & Customs
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

import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.WSResponse
import support.IntegrationBaseSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

import scala.util.Try

class DocumentationControllerISpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, String] = super.servicesConfig + ("feature-switch.generate_token.enabled" -> "true")

  val config: AppConfig                = app.injector.instanceOf[AppConfig]
  val confidenceLevel: ConfidenceLevel = config.confidenceLevelConfig.confidenceLevel

  val apiDefinitionJson: JsValue = Json.parse(
    s"""
       |{
       |   "api":{
       |      "name":"Self Assessment Test Support (MTD)",
       |      "description":"An API providing test support for MTD Self Assessment APIs",
       |      "context":"individuals/self-assessment-test-support",
       |      "categories":[
       |         "INCOME_TAX_MTD"
       |      ],
       |      "versions":[
       |         {
       |            "version":"1.0",
       |            "status":"BETA",
       |            "endpointsEnabled":true
       |         }
       |      ],
       |      "isTestSupport":true
       |   }
       |}
    """.stripMargin
  )

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

  "an OAS documentation request for V1" must {
    "serve internal authorization documentation when the feature is enabled" in {
      await(buildRequest("/api/conf/1.0/auth_generate.yaml").get()).status shouldBe Status.OK
    }

    "allow internal authorization requests when the feature is enabled" in {
      val response =
        await(buildRequest("/auth/generate").withHttpHeaders("Content-Type" -> "application/json").post(Json.obj("identifier" -> "missing-nino")))

      response.status shouldBe Status.BAD_REQUEST
      response.body should include("obj.nino")
    }

    "resolve the internal authorization endpoint, schemas and examples" in {
      val options = new ParseOptions
      options.setResolve(true)
      options.setResolveFully(true)
      val document  = new OpenAPIV3Parser().read(s"http://localhost:$port/api/conf/1.0/application.yaml", null, options)
      val operation = document.getPaths.get("/auth/generate").getPost

      operation.getSummary shouldBe "Generate a Test OAuth Token"
      operation.getSecurity.isEmpty shouldBe true
      operation.getServers.get(0).getUrl shouldBe "http://localhost:12221"

      val request = operation.getRequestBody.getContent.get("application/json")
      request.getSchema.getRequired should contain("nino")
      request.getSchema.getProperties.keySet() should contain allOf ("nino", "identifier")
      request.getExamples.size() shouldBe 2

      val response = operation.getResponses.get("200").getContent.get("application/json")
      response.getSchema.getRequired should contain("Authorization")
      response.getSchema.getProperties.keySet() should contain("Authorization")
      Seq("400", "404", "415", "422", "500").foreach { code =>
        operation.getResponses.containsKey(code) shouldBe true
      }

      val errors = operation.getResponses.get("422").getContent.get("application/json").getExamples
      errors.get("GRANTSCOPE_NOT_FOUND").getValue should not be null
      errors.get("OAUTH_CODE_RETRIEVAL_ERROR").getValue should not be null
    }

    "return the documentation that passes OAS V3 parser" in {
      val response: WSResponse = await(buildRequest("/api/conf/1.0/application.yaml").get())
      response.status shouldBe Status.OK

      val contents     = response.body[String]
      val parserResult = Try(new OpenAPIV3Parser().readContents(contents))
      parserResult.isSuccess shouldBe true

      val openAPI = Option(parserResult.get.getOpenAPI)
      openAPI.isEmpty shouldBe false
      openAPI.get.getOpenapi shouldBe "3.0.3"
      openAPI.get.getInfo.getTitle shouldBe "Self Assessment Test Support (MTD)"
      openAPI.get.getInfo.getVersion shouldBe "1.0"
    }
  }

}

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

package api.support

import api.services.{AuthStub, DownstreamStub}
import api.utils.UnitSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.test.WireMockSupport

trait IntegrationBaseSpec extends UnitSpec with WireMockSupport with DownstreamStub with AuthStub with GuiceOneServerPerSuite {

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def servicesConfig: Map[String, String] = Map(
    "microservice.services.stub.host" -> wireMockHost,
    "microservice.services.stub.port" -> wireMockPort.toString,
    "microservice.services.auth.host" -> wireMockHost,
    "microservice.services.auth.port" -> wireMockPort.toString,
    "auditing.consumer.baseUri.port"  -> wireMockPort.toString
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  def buildRequest(path: String): WSRequest = client.url(s"http://localhost:$port$path").withFollowRedirects(false)

  def document(response: WSResponse): JsValue = Json.parse(response.body)

  def downstreamErrorBody(code: String): JsValue = Json.parse(
    s"""
      |{
      |   "code": "$code",
      |   "reason": "Downstream message"
      |}
    """.stripMargin
  )

}

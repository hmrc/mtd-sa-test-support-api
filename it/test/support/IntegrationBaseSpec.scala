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

package support

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}
import stubs.{AuthStub, DownstreamStub}
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

  /** Creates downstream request body by reading JSON and then writing it back via a model class `A` */
  def downstreamBody[A: Format](json: JsValue): JsValue = Json.toJson(json.as[A])

  /** Creates downstream request body by reading JSON and then writing it back via a model class `A` */
  def downstreamBody[A: Format](json: String): String = downstreamBody(Json.parse(json)).toString()

  def buildRequest(path: String): WSRequest = client.url(s"http://localhost:$port$path").withFollowRedirects(false)

  def document(response: WSResponse): JsValue = Json.parse(response.body)

  override def beforeEach(): Unit = {
    super.beforeEach()

    // To silence errors about implicit audits
    when(POST, "/write/audit/merged").thenReturnNoContent()
    when(POST, "/write/audit").thenReturnNoContent()
  }

  def downstreamErrorBody(code: String): JsValue = Json.parse(s"""
       |{
       |   "code": "$code",
       |   "reason": "Downstream message"
       |}
    """.stripMargin)

}

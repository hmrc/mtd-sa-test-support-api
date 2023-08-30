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

package uk.gov.hmrc.mtdsatestsupportapi.connectors

import api.connectors.ConnectorSpec
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.CREATED
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.mtdsatestsupportapi.fixtures.CreateBusinessFixtures
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness.CreateBusinessRequest

class CreateBusinessConnectorSpec extends ConnectorSpec with CreateBusinessFixtures {

  import ExampleCreateBusinessResponse._
  import MinimalCreateBusinessRequest._

  private val nino    = "AA123456A"
  private val request = CreateBusinessRequest(Nino(nino), business)

  trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateBusinessConnector(httpClientV2, mockAppConfig)
  }

  "CreateBusinessConnector" when {
    "the downstream returns a successful 201 response" must {
      "return a success result with the businessId" in new StubTest with Test {
        when(POST, s"/test-support/business-details/$nino")
          .withRequestBody(downstreamBusinessJson)
          .withHeaders(requiredHeaders)
          .thenReturn[JsValue](CREATED, headers = responseHeaders, body = downstreamResponseJson)

        await(connector.createBusiness(request)) shouldBe Right(ResponseWrapper(responseCorrelationId, response))
      }
    }

    "the downstream response is an error" must {
      "return a failure result" in new StubTest with Test {
        when(POST, s"/test-support/business-details/$nino")
          .withRequestBody(downstreamBusinessJson)
          .withHeaders(requiredHeaders)
          .thenReturn(400, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some message"), responseHeaders)

        await(connector.createBusiness(request)) shouldBe Left(
          ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }

}

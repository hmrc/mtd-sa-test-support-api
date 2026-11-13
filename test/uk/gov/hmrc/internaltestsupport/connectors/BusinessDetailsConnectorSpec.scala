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

package uk.gov.hmrc.internaltestsupport.connectors

import api.connectors.ConnectorSpec
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.internaltestsupport.models.MtdIdReference

class BusinessDetailsConnectorSpec extends ConnectorSpec {

  "BusinessDetailsConnector" should {
    "return the MTD ID for the requested NINO" in new Test {
      when(GET, s"/etmp/RESTAdapter/itsa/taxpayer/business-details")
        .withQueryParams(Seq("nino" -> nino))
        .withHeaders(Seq(
          "Authorization"         -> s"Basic $basicAuth",
          "CorrelationId"         -> requestCorrelationId,
          "Environment"           -> hipEnv,
          "X-Message-Type"        -> "TaxpayerDisplay",
          "X-Originating-System"  -> "MDTP",
          "X-Regime-Type"         -> "ITSA",
          "X-Transmitting-System" -> "HIP"
        ))
        .thenReturn[JsValue](status = 200, body = response, headers = responseHeaders)

      await(connector.getMtdId(nino)) shouldBe Right(ResponseWrapper(responseCorrelationId, MtdIdReference(mtdId)))
    }

    "return downstream errors when the lookup fails" in new Test {
      when(GET, s"/etmp/RESTAdapter/itsa/taxpayer/business-details")
        .withQueryParams(Seq("nino" -> nino))
        .thenReturn[JsValue](status = 404, body = Json.obj("code" -> "NOT_FOUND"))

      await(connector.getMtdId(nino)) shouldBe Left(ResponseWrapper("", DownstreamErrors.single(DownstreamErrorCode("NOT_FOUND"))))
    }
  }

  trait Test extends ConnectorTest {
    protected val nino      = "AA123456A"
    protected val mtdId     = "XQIT12345678901"
    protected val hipEnv    = "HIP-QA"
    protected val basicAuth = java.util.Base64.getEncoder.encodeToString("client-id:client-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8))
    protected val response  = Json.obj("success" -> Json.obj("taxPayerDisplayResponse" -> Json.obj("mtdId" -> mtdId)))
    protected val connector = new BusinessDetailsConnector(httpClientV2, mockAppConfig)

    MockAppConfig.stubEnv returns stubEnv
    MockAppConfig.stubToken returns stubToken

    MockAppConfig.hipBaseUrl returns baseUrl
    MockAppConfig.hipEnv returns hipEnv
    MockAppConfig.hipClientId returns "client-id"
    MockAppConfig.hipClientSecret returns "client-secret"
    MockAppConfig.hipEnvironmentHeaders returns None

  }

}

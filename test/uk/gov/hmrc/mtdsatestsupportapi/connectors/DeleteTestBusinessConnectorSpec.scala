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
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteTestBusiness.DeleteTestBusinessRequest

class DeleteTestBusinessConnectorSpec extends ConnectorSpec {

  "DeleteTestBusinessConnectorSpec" must {
    "return 204" when {
      "a success response is received from downstream" in new StubTest with Test {
        when(method = DELETE, uri = downstreamUri).withHeaders(requiredHeaders).thenReturnNoContent(headers = responseHeaders)
        await(connector.deleteTestBusiness(requestData)) shouldBe Right(ResponseWrapper(responseCorrelationId, ()))
      }
    }
    "return the error" when {
      "an error is received from the downstream" in new StubTest with Test {
        when(method = DELETE, uri = downstreamUri)
          .withHeaders(requiredHeaders)
          .thenReturn(BAD_REQUEST, Json.obj("code" -> "SOME_ERROR", "reason" -> "Some explanation"), responseHeaders)

      await(connector.deleteTestBusiness(requestData)) shouldBe Left(
        ResponseWrapper(responseCorrelationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR")))
      )
      }
    }
  }

  trait Test {
    _: ConnectorTest =>
    protected val vendorClientId = "some_vendor_id"
    protected val nino           = "AA999999A"
    protected val businessId     = "XAIS12345678910"
    protected val downstreamUri  = s"/test-support/business-details/$nino/$businessId"

    protected val requestData = DeleteTestBusinessRequest(vendorClientId, Nino(nino), businessId)

    protected val connector = new DeleteTestBusinessConnector(mockAppConfig, httpClientV2)

  }

}

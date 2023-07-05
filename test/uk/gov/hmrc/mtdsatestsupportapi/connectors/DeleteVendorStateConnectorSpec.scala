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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRequest

import scala.concurrent.Future

class DeleteVendorStateConnectorSpec extends ConnectorSpec {
  private val vendorId        = "someVendor"
  private val nino            = "AA123456A"
  private val request         = DeleteStatefulTestDataRequest(vendorId, None)
  private val requestWithNino = DeleteStatefulTestDataRequest(vendorId, Some(Nino(nino)))

  trait Test {
    _: ConnectorTest =>

    protected val connector: DeleteVendorStateConnector = new DeleteVendorStateConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willDelete(url = s"$baseUrl/test-support/vendor-state/$vendorId") returns Future.successful(outcome)
    }

    protected def stubHttpResponseWithNino(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willDelete(url = s"$baseUrl/test-support/vendor-state/$vendorId?taxableEntityId=$nino") returns Future.successful(outcome)
    }

  }

  "deleteVendorState" when {
    "the downstream returns a successful response" must {
      "return a successful result" in new StubTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        stubHttpResponse(outcome)

        await(connector.deleteVendorState(request)) shouldBe outcome
      }
      "return a successful result with nino" in new StubTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        stubHttpResponseWithNino(outcome)

        await(connector.deleteVendorState(requestWithNino)) shouldBe outcome
      }
    }

    "the downstream response is an error" must {
      "return a failure result" in new StubTest with Test {
        val downstreamErrorResponse: DownstreamErrors =
          DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        stubHttpResponse(outcome)

        await(connector.deleteVendorState(request)) shouldBe outcome
      }
    }
  }

}

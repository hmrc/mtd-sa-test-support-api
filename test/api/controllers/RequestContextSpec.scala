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

package api.controllers

import api.utils.{MockIdGenerator, UnitSpec}
import uk.gov.hmrc.http.HeaderCarrier

class RequestContextSpec extends UnitSpec with MockIdGenerator {

  private val endpointLogContext: EndpointLogContext = EndpointLogContext("controllerName", "endpointName")

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val expectedCorrelationId: String = "1234567890"

  "from" should {
    "return a RequestContext" in {
      MockedIdGenerator.getCorrelationId.returns(expectedCorrelationId).anyNumberOfTimes()

      val result: RequestContext = RequestContext.from(mockIdGenerator, endpointLogContext)

      result shouldBe RequestContext(hc, expectedCorrelationId, endpointLogContext)
    }
  }

  "fromParts" should {
    "return a RequestContext" in {
      val result: RequestContext = RequestContext.fromParts(hc, "12345", endpointLogContext)

      result shouldBe RequestContext(hc, "12345", endpointLogContext)
    }
  }

  "withCorrelationId" should {
    "return a copy of the RequestContext with the new correlation ID" in {
      MockedIdGenerator.getCorrelationId.returns("some-correlation-id").anyNumberOfTimes()

      val requestContext: RequestContext = RequestContext.from(mockIdGenerator, endpointLogContext)

      val result: RequestContext = requestContext.withCorrelationId("new-correlation-id")

      result shouldBe RequestContext(hc, "new-correlation-id", endpointLogContext)
    }
  }

}

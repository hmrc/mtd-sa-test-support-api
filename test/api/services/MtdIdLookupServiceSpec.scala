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

package api.services

import api.mocks.connectors.MockMtdIdLookupConnector
import api.models.errors.{InternalError, NinoFormatError, ClientNotAuthenticatedError}

import scala.concurrent.Future

class MtdIdLookupServiceSpec extends ServiceSpec {

  trait Test extends MockMtdIdLookupConnector {
    lazy val target = new MtdIdLookupService(mockMtdIdLookupConnector)
  }

  val nino: String        = "AA123456A"
  val invalidNino: String = "INVALID_NINO"

  "calling .getMtdId" when {

    "an invalid NINO is passed in" should {
      "return a valid mtdId" in new Test {

        val expected = Left(NinoFormatError)

        // should not call the connector
        MockedMtdIdLookupConnector
          .lookup(invalidNino)
          .never()

        private val result = await(target.lookup(invalidNino))

        result shouldBe expected
      }
    }

    "a not authorised error occurs the service" should {
      "proxy the error to the caller" in new Test {
        val connectorResponse = Left(ClientNotAuthenticatedError)

        MockedMtdIdLookupConnector
          .lookup(nino)
          .returns(Future.successful(connectorResponse))

        private val result = await(target.lookup(nino))

        result shouldBe connectorResponse
      }
    }

    "a downstream error occurs the service" should {
      "proxy the error to the caller" in new Test {
        val connectorResponse = Left(InternalError)

        MockedMtdIdLookupConnector
          .lookup(nino)
          .returns(Future.successful(connectorResponse))

        private val result = await(target.lookup(nino))

        result shouldBe connectorResponse
      }
    }

  }
}

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

package uk.gov.hmrc.internaltestsupport.services

import api.connectors.DownstreamOutcome
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, InternalError, NotFoundError, OutboundError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.internaltestsupport.connectors.BusinessDetailsConnector
import uk.gov.hmrc.internaltestsupport.models.MtdIdReference

import scala.concurrent.Future

class LookupServiceSpec extends ServiceSpec with MockFactory {

  private val connector                             = mock[BusinessDetailsConnector]
  private val service                               = new LookupService(connector)
  private val nino                                  = "AA123456A"
  private val mtdId                                 = MtdIdReference("XQIT12345678901")
  private implicit val requestCorrelationId: String = correlationId

  "LookupService" should {
    "return the MTD ID from a successful connector response" in {
      expectLookup(Right(ResponseWrapper(correlationId, mtdId)))

      await(service.getMtdId(nino)) shouldBe Right(mtdId)
    }

    "map a not-found response to NotFoundError" in {
      expectLookup(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("NOT_FOUND")))))

      await(service.getMtdId(nino)) shouldBe Left(NotFoundError)
    }

    "map other downstream errors to InternalError" in {
      expectLookup(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SERVER_ERROR")))))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }

    "preserve an explicitly mapped not-found error" in {
      expectLookup(Left(ResponseWrapper(correlationId, OutboundError(NotFoundError))))

      await(service.getMtdId(nino)) shouldBe Left(NotFoundError)
    }

    "not hide a technical failure when a response also contains NOT_FOUND" in {
      expectLookup(
        Left(ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("NOT_FOUND"), DownstreamErrorCode("SERVER_ERROR"))))))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }

    "map an empty downstream error list to InternalError" in {
      expectLookup(Left(ResponseWrapper(correlationId, DownstreamErrors(Nil))))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }

    "not hide a technical failure within a mapped not-found response" in {
      expectLookup(Left(ResponseWrapper(correlationId, OutboundError(NotFoundError, Some(Seq(InternalError))))))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }

    "recognize a not-found error with a customized message" in {
      expectLookup(Left(ResponseWrapper(correlationId, OutboundError(NotFoundError.copy(message = "Individual not found")))))

      await(service.getMtdId(nino)) shouldBe Left(NotFoundError)
    }

    "map a failed connector future to InternalError" in {
      (connector
        .getMtdId(_: String)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
        .expects(nino, *, *, *)
        .returning(Future.failed(new java.net.ConnectException("connection failed")))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }

    "map a synchronous connector exception to InternalError" in {
      (connector
        .getMtdId(_: String)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
        .expects(nino, *, *, *)
        .throwing(new IllegalStateException("configuration failed"))

      await(service.getMtdId(nino)) shouldBe Left(InternalError)
    }
  }

  private def expectLookup(outcome: DownstreamOutcome[MtdIdReference]): Unit =
    (connector
      .getMtdId(_: String)(_: uk.gov.hmrc.http.HeaderCarrier, _: scala.concurrent.ExecutionContext, _: String))
      .expects(nino, *, *, *)
      .returning(Future.successful(outcome))

}

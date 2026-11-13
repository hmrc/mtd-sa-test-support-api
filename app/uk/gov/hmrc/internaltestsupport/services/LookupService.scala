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

import api.models.errors.{DownstreamErrors, InternalError, MtdError, NotFoundError, OutboundError}
import api.models.outcomes.ResponseWrapper
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.internaltestsupport.connectors.BusinessDetailsConnector
import uk.gov.hmrc.internaltestsupport.models.{MtdIdReference}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class LookupService @Inject() (connector: BusinessDetailsConnector) extends Logging {

  def getMtdId(nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdReference]] = {
    Future.unit
      .flatMap(_ => connector.getMtdId(nino))
      .map {
        case Right(response) =>
          Right(response.responseData)
        case Left(ResponseWrapper(_, OutboundError(error, errors)))
            if error.code == NotFoundError.code && errors.forall(_.forall(_.code == NotFoundError.code)) =>
          Left(NotFoundError)
        case Left(ResponseWrapper(_, DownstreamErrors(errors)))
            if errors.nonEmpty && errors.forall(error => error.code == NotFoundError.code || error.code == "NOT_FOUND") =>
          Left(NotFoundError)
        case Left(_) =>
          logger.error(s"[LookupService] MTD ID lookup failed [correlationId=$correlationId]")
          Left(InternalError)
      }
      .recover { case NonFatal(e) =>
        logger.error(s"[LookupService] MTD ID lookup raised ${e.getClass.getSimpleName} [correlationId=$correlationId]")
        Left(InternalError)
      }
  }

}

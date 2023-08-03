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

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.CreateCheckpointResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateCheckpointConnector extends MockFactory {

  val mockCreateCheckpointConnector: CreateCheckpointConnector =
    mock[CreateCheckpointConnector]

  object MockCreateCheckpointConnector {

    def createCheckpoint(requestData: CreateCheckpointRequest): CallHandler[Future[DownstreamOutcome[CreateCheckpointResponse]]] =
      (mockCreateCheckpointConnector
        .createCheckpoint(_: CreateCheckpointRequest)(
          _: HeaderCarrier,
          _: ExecutionContext,
          _: String
        ))
        .expects(requestData, *, *, *)

  }

}

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

package uk.gov.hmrc.mtdsatestsupportapi.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mtdsatestsupportapi.connectors.DeleteCheckpointConnector
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteCheckpoint.DeleteCheckpointRequest

import scala.concurrent._
trait MockDeleteCheckpointConnector extends TestSuite with MockFactory {

  val mockDeleteCheckpointConnector: DeleteCheckpointConnector = mock[DeleteCheckpointConnector]

  object MockDeleteCheckpointConnector {

    def deleteCheckpoint(requestData: DeleteCheckpointRequest): CallHandler[Future[DownstreamOutcome[Unit]]] =
      (mockDeleteCheckpointConnector
        .deleteCheckpoint(_: DeleteCheckpointRequest)(
          _: ExecutionContext,
          _: HeaderCarrier,
          _: String
        ))
        .expects(requestData, *, *, *)

  }

}

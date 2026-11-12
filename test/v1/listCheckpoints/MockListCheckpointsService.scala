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

package v1.listCheckpoints

import api.controllers.RequestContext
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v1.listCheckpoints.model.request.ListCheckpointsRequestData
import v1.listCheckpoints.model.response.ListCheckpointsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockListCheckpointsService extends TestSuite with MockFactory {

  val mockListCheckpointsService: ListCheckpointsService = mock[ListCheckpointsService]

  object MockListCheckpointsService {

    def listCheckpoints(request: ListCheckpointsRequestData): CallHandler[Future[ServiceOutcome[ListCheckpointsResponse]]] =
      (mockListCheckpointsService.listCheckpoints(_: ListCheckpointsRequestData)(_: ExecutionContext, _: RequestContext)).expects(request, *, *)

  }

}

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

package v1.deleteStatefulTestData

import api.controllers.RequestContext
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v1.deleteStatefulTestData.model.request.DeleteStatefulTestDataRequestData

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteStatefulTestDataService extends TestSuite with MockFactory {

  val mockDeleteStatefulTestDataService: DeleteStatefulTestDataService = mock[DeleteStatefulTestDataService]

  object MockDeleteStatefulTestDataService {

    def deleteStatefulTestData(request: DeleteStatefulTestDataRequestData): CallHandler[Future[ServiceOutcome[Unit]]] =
      (mockDeleteStatefulTestDataService
        .deleteStatefulTestData(_: DeleteStatefulTestDataRequestData)(_: ExecutionContext, _: RequestContext))
        .expects(request, *, *)

  }

}

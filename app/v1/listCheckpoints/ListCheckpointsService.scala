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
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v1.listCheckpoints.model.request.ListCheckpointsRequestData
import v1.listCheckpoints.model.response.ListCheckpointsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListCheckpointsService @Inject() (connector: ListCheckpointsConnector) extends BaseService {

  def listCheckpoints(
      request: ListCheckpointsRequestData)(implicit ec: ExecutionContext, rc: RequestContext): Future[ServiceOutcome[ListCheckpointsResponse]] =
    connector.listCheckpoints(request).map(_.leftMap(mapDownstreamErrors(Map.empty)))

}

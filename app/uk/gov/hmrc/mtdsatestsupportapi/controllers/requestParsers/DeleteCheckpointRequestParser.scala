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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import api.models.domain.CheckpointId
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.DeleteCheckpointValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteCheckpoint.{DeleteCheckpointRawData, DeleteCheckpointRequest}

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteCheckpointRequestParser @Inject() (val validator: DeleteCheckpointValidator)
    extends RequestParser[DeleteCheckpointRawData, DeleteCheckpointRequest] {

  override protected def requestFor(data: DeleteCheckpointRawData): DeleteCheckpointRequest =
    DeleteCheckpointRequest(data.vendorClientId, CheckpointId(data.checkpointId))

}

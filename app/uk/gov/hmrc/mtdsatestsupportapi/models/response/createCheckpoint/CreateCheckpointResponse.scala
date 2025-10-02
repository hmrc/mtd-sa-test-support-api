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

package uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.{CheckpointId, Nino}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.mtdsatestsupportapi.models.hateoas.CheckpointHateoasLinks

case class CreateCheckpointResponse(checkpointId: CheckpointId)

object CreateCheckpointResponse extends CheckpointHateoasLinks {
  implicit val formats: OFormat[CreateCheckpointResponse] = Json.format

  implicit object LinksFactory extends HateoasLinksFactory[CreateCheckpointResponse, CreateCheckpointHateoasData] {

    override def links(appConfig: AppConfig, data: CreateCheckpointHateoasData): Seq[Link] = {
      import data.*
      Seq(
        deleteCheckpoint(appConfig, checkpointId),
        restoreCheckpoint(appConfig, checkpointId),
        listCheckpoints(appConfig, Some(nino))
      )
    }

  }

}

case class CreateCheckpointHateoasData(nino: Nino, checkpointId: CheckpointId) extends HateoasData

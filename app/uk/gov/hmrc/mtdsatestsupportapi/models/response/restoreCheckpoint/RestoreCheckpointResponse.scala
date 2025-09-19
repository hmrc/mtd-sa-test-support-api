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

package uk.gov.hmrc.mtdsatestsupportapi.models.response.restoreCheckpoint

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.CheckpointId
import config.AppConfig
import uk.gov.hmrc.mtdsatestsupportapi.models.hateoas.CheckpointHateoasLinks

object RestoreCheckpointResponse extends CheckpointHateoasLinks {

  implicit object RestoreCheckpointLinksFactory extends HateoasLinksFactory[Unit, RestoreCheckpointHateoasData] {

    override def links(appConfig: AppConfig, data: RestoreCheckpointHateoasData): Seq[Link] = {
      import data.*
      Seq(
        deleteCheckpoint(appConfig, CheckpointId(checkpointId))
      )
    }

  }

}

case class RestoreCheckpointHateoasData(checkpointId: String) extends HateoasData

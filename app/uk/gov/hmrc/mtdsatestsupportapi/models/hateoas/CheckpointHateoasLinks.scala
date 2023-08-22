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

package uk.gov.hmrc.mtdsatestsupportapi.models.hateoas

import api.hateoas.Link
import config.AppConfig
import api.hateoas.Method._
import api.models.domain.{CheckpointId, Nino}

trait CheckpointHateoasLinks {

  val SELF = "self"

  private def baseUrl(appConfig: AppConfig) =
    s"/${appConfig.apiGatewayContext}/vendor-state/checkpoints"

  def createCheckpoint(appConfig: AppConfig, nino: Nino): Link =
    Link(href = s"${baseUrl(appConfig)}/?nino=${nino.value}", method = POST, rel = "create-checkpoint")

  def restoreCheckpoint(appConfig: AppConfig, checkpointId: CheckpointId): Link =
    Link(href = s"${baseUrl(appConfig)}/${checkpointId.value}/restore", method = POST, rel = "restore-checkpoint")

  def deleteCheckpoint(appConfig: AppConfig, checkpointId: CheckpointId): Link =
    Link(href = s"${baseUrl(appConfig)}/${checkpointId.value}", method = DELETE, rel = "delete-checkpoint")

  def listCheckpoints(appConfig: AppConfig, nino: Option[Nino]): Link = {
    val url = nino match {
      case Some(nino) => s"${baseUrl(appConfig)}?nino=${nino.value}"
      case None       => s"${baseUrl(appConfig)}"
    }

    Link(href = url, method = GET, rel = "list-checkpoints")
  }

}

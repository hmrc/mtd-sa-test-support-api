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

package uk.gov.hmrc.mtdsatestsupportapi.models.response.listCheckpoints

import api.hateoas._
import api.models.domain.{CheckpointId, Nino}
import cats.Functor
import config.AppConfig
import play.api.libs.json._
import uk.gov.hmrc.mtdsatestsupportapi.models.hateoas.CheckpointHateoasLinks

case class ListCheckpointsResponse[I](checkpoints: Seq[I])

object ListCheckpointsResponse extends CheckpointHateoasLinks {

  implicit def reads: Reads[ListCheckpointsResponse[Checkpoint]] = Json.reads[ListCheckpointsResponse[Checkpoint]]

  implicit def writes[I: Writes]: OWrites[ListCheckpointsResponse[I]] = Json.writes[ListCheckpointsResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListCheckpointsResponse, Checkpoint, ListCheckpointsHateoasData.type] {

    override def itemLinks(appConfig: AppConfig, data: ListCheckpointsHateoasData.type, item: Checkpoint): Seq[Link] = {
      val maybeCreateHateoas = item.nino match {
        case Some(nino) => Seq(createCheckpoint(appConfig, Nino(nino)))
        case None       => Seq.empty
      }
      maybeCreateHateoas ++ Seq(
        deleteCheckpoint(appConfig, CheckpointId(item.checkpointId)),
        restoreCheckpoint(appConfig, CheckpointId(item.checkpointId))
      )
    }

    override def links(appConfig: AppConfig, data: ListCheckpointsHateoasData.type): Seq[Link] = Seq.empty
  }

  implicit object ResponseFunctor extends Functor[ListCheckpointsResponse] {

    override def map[A, B](fa: ListCheckpointsResponse[A])(f: A => B): ListCheckpointsResponse[B] =
      ListCheckpointsResponse(fa.checkpoints.map(f))

  }

}

case object ListCheckpointsHateoasData extends HateoasData

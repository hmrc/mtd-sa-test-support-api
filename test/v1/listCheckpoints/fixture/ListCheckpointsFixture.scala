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

package v1.listCheckpoints.fixture

import play.api.libs.json.{JsValue, Json}
import v1.listCheckpoints.model.response.{Checkpoint, ListCheckpointsResponse}

object ListCheckpointsFixture {

  val checkpointId: String                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  val nino: String                        = "AA123456A"
  val checkpointCreationTimestamp: String = "2019-01-01T00:00:00.000Z"

  val checkpointModel: Checkpoint = Checkpoint(
    checkpointId = checkpointId,
    nino = Some(nino),
    checkpointCreationTimestamp = checkpointCreationTimestamp
  )

  val responseBodyModel: ListCheckpointsResponse = ListCheckpointsResponse(Seq(checkpointModel))

  val downstreamCheckpointJson: JsValue = Json.parse(
    s"""
      |{
      |  "checkpointId": "$checkpointId",
      |  "taxableEntityId": "$nino",
      |  "checkpointCreationTimestamp": "$checkpointCreationTimestamp"
      |}
    """.stripMargin
  )

  val downstreamResponseBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "checkpoints": [$downstreamCheckpointJson]
      |}
    """.stripMargin
  )

  val mtdCheckpointJson: JsValue = Json.parse(
    s"""
      |{
      |  "checkpointId": "$checkpointId",
      |  "nino": "$nino",
      |  "checkpointCreationTimestamp": "$checkpointCreationTimestamp"
      |}
    """.stripMargin
  )

  val mtdResponseBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "checkpoints": [$mtdCheckpointJson]
      |}
    """.stripMargin
  )

}

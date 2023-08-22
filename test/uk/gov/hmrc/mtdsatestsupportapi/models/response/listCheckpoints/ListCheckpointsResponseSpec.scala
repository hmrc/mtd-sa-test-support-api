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

import play.api.libs.json._
import support.UnitSpec

class ListCheckpointsResponseSpec extends UnitSpec {

  "ListCheckpointsResponse" should {
    "deserialize valid JSON" in new Test {
      Json.fromJson(validDownstreamResponseJson) shouldBe JsSuccess(
        ListCheckpointsResponse(Seq(Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp))))
    }
    "serialize JSON" in new Test {
      Json.toJson(response) shouldBe validMtdResponseJson
    }
  }

  trait Test {

    protected val checkpointId                = "some_checkpoint_id"
    protected val nino                        = "some_nino"
    protected val checkpointCreationTimestamp = "2019-01-01T00:00:00.000Z"

    protected val response: ListCheckpointsResponse[Checkpoint] =
      ListCheckpointsResponse(Seq(Checkpoint(checkpointId, Some(nino), checkpointCreationTimestamp)))

    protected val validDownstreamResponseJson: JsValue = Json.parse(s"""
         |{
         |  "checkpoints": [
         |    {
         |      "checkpointId": "$checkpointId",
         |      "taxableEntityId": "$nino",
         |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp"
         |    }
         |  ]
         |}
         |""".stripMargin)

    protected val validMtdResponseJson: JsValue = Json.parse(s"""
         |{
         |  "checkpoints": [
         |    {
         |      "checkpointId": "$checkpointId",
         |      "nino": "$nino",
         |      "checkpointCreationTimestamp": "$checkpointCreationTimestamp"
         |    }
         |  ]
         |}
         |""".stripMargin)

    implicit val reads: Reads[ListCheckpointsResponse[Checkpoint]] = ListCheckpointsResponse.reads
  }

}

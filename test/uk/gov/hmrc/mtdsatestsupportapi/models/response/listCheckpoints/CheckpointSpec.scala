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

class CheckpointSpec extends UnitSpec {

  protected val response: Checkpoint = Checkpoint("some_id", Some("some_nino"), "2019-01-01T00:00:00.000Z")

  protected val validDownstreamResponseJson: JsObject =
    Json.obj("checkpointId" -> "some_id", "taxableEntityId" -> "some_nino", "checkpointCreationTimestamp" -> "2019-01-01T00:00:00.000Z")

  protected val validMtdResponseJson: JsObject =
    Json.obj("checkpointId" -> "some_id", "nino" -> "some_nino", "checkpointCreationTimestamp" -> "2019-01-01T00:00:00.000Z")

  protected val invalidDownstreamJson: JsObject = Json.obj("checkpointId" -> "some_id", "taxableEntityId" -> "some_nino")

  implicit val reads: Reads[Checkpoint]    = Checkpoint.reads
  implicit val writes: OWrites[Checkpoint] = Checkpoint.writes

  "Checkpoint" when {
    "deserializing valid JSON" should {
      "create the response object" in {
        Json.fromJson(validDownstreamResponseJson) shouldBe JsSuccess(response)
      }
    }
    "deserializing invalid JSON" should {
      "return a failed result" in {
        Json.fromJson(invalidDownstreamJson) shouldBe JsError(
          List((JsPath \ "checkpointCreationTimestamp", List(JsonValidationError(List("error.path.missing"))))))
      }
    }
    "serializing JSON" in {
      Json.toJson(response) shouldBe validMtdResponseJson
    }
  }

}

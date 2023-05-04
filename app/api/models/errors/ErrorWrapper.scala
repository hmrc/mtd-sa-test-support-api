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

package api.models.errors

import play.api.libs.json.{ JsObject, Json, Writes }
import api.models.audit.AuditError

case class ErrorWrapper(correlationId: String, error: MtdError, errors: Option[Seq[MtdError]] = None) {

  private val allErrors: Seq[MtdError] = errors match {
    case Some(seq) => seq
    case None      => Seq(error)
  }

  def auditErrors: Seq[AuditError] =
    allErrors.map(error => AuditError(error.code))

  /** Controller only checks the first/main error code, not the additional errors.
    */
  def containsAnyOf(errorsToCheck: MtdError*): Boolean =
    errorsToCheck.exists(_.code == error.code)
}

object ErrorWrapper {
  implicit val writes: Writes[ErrorWrapper] = (errorResponse: ErrorWrapper) => {

    val json = Json.toJson(errorResponse.error).as[JsObject]

    errorResponse.errors match {
      case Some(errors) if errors.nonEmpty => json + ("errors" -> Json.toJson(errors))
      case _                               => json
    }

  }

}

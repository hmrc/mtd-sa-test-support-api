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

package api.models.errors

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsObject, JsPath, Json, OWrites}

case class MtdError(code: String, message: String, httpStatus: Int, paths: Option[Seq[String]] = None) {
  val asJson: JsObject = Json.toJson(this).as[JsObject]

  /** Returns a copy of this error with the path, replacing any that were already present.
    */
  def withPath(path: String): MtdError = copy(paths = Some(List(path)))

  /** Returns a copy of this error with the paths, replacing any that were already present.
    */
  def withPaths(paths: Seq[String]): MtdError = copy(paths = Some(paths))

  /** If maybePath is defined, returns a copy of this error with the path, replacing any that were already present; otherwise returns the original
    * error.
    */
  def maybeWithPath(maybePath: Option[String]): MtdError =
    maybePath.map(withPath).getOrElse(this)

  /** Returns a copy of this error with an additional path.
    */
  def withExtraPath(newPath: String): MtdError =
    paths.fold(
      copy(paths = Some(List(newPath)))
    ) { existingPaths =>
      copy(paths = Some(existingPaths :+ newPath))
    }

  def maybeWithExtraPath(maybeNewPath: Option[String]): MtdError =
    maybeNewPath.map(withExtraPath).getOrElse(this)

}

object MtdError {

  implicit val writes: OWrites[MtdError] = (
    (JsPath \ "code").write[String] and
      (JsPath \ "message").write[String] and
      (JsPath \ "paths").writeNullable[Seq[String]]
  )(unlift(MtdError.unapply))

  // excludes httpStatus
  def unapply(e: MtdError): Option[(String, String, Option[Seq[String]])] = Some((e.code, e.message, e.paths))

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)

}

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

package api.controllers.validators.resolvers

import api.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import api.utils.Logging
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.*

class ResolveJsonObjectInternal[A](implicit val reads: Reads[A]) extends ResolverSupport with Logging {

  val resolver: Resolver[JsValue, (JsObject, A)] = {
    case jsObj: JsObject =>
      if (jsObj.fields.isEmpty) {
        Invalid(List(RuleIncorrectOrEmptyBodyError))
      } else {
        jsObj.validate[A] match {
          case JsSuccess(parsed, _) => Valid((jsObj, parsed))
          case JsError(errors) =>
            val immutableErrors = errors.map { (path, errors) => (path, errors.toList) }.toList
            Invalid(toMtdError(immutableErrors))
        }
      }

    case _ =>
      Invalid(List(RuleIncorrectOrEmptyBodyError))
  }

  private def toMtdError(errors: Seq[(JsPath, Seq[JsonValidationError])]): Seq[MtdError] = {
    val failures = errors.map {
      case (path, List(JsonValidationError(List("error.path.missing"))))                              => MissingMandatoryField(path)
      case (path, List(JsonValidationError(List(error: String)))) if error.contains("error.expected") => WrongFieldType(path)
      case (path, _)                                                                                  => OtherFailure(path)
    }

    log(failures)
    List(RuleIncorrectOrEmptyBodyError.withPaths(failures.map(_.errorPath).sorted))
  }

  private def log(failures: Seq[JsonFormatValidationFailure]): Unit = {
    val logString = failures
      .groupBy(_.getClass)
      .values
      .map(failures => s"${failures.head.failureReason}: " + s"${failures.map(_.errorPath)}")
      .mkString(", ")

    logger.warn(s"Request body failed validation with errors - $logString")
  }

  private class JsonFormatValidationFailure(jsPath: JsPath, val failureReason: String) {

    def errorPath: String =
      jsPath.path.map {
        case IdxPathNode(idx) => s"/$idx"
        case node: PathNode   => node.toString
      }.mkString

  }

  private case class MissingMandatoryField(path: JsPath) extends JsonFormatValidationFailure(path, "Missing mandatory field")
  private case class WrongFieldType(path: JsPath)        extends JsonFormatValidationFailure(path, "Wrong field type")
  private case class OtherFailure(path: JsPath)          extends JsonFormatValidationFailure(path, "Other failure")
}

object ResolveJsonObjectInternal extends ResolverSupport {

  def resolver[T: Reads]: Resolver[JsValue, (JsObject, T)] = new ResolveJsonObjectInternal[T].resolver

}

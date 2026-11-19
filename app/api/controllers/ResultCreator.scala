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

package api.controllers

import play.api.http.{HttpEntity, Status}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{ResponseHeader, Result, Results}

case class ResultWrapper(httpStatus: Int, body: Option[JsValue]) {

  def asResult: Result = {
    body match {
      case Some(b) => Results.Status(httpStatus)(b)
      case None    => Result(header = ResponseHeader(httpStatus), body = HttpEntity.NoEntity)
    }
  }

}

trait ResultCreator[Input, Output] {

  def createResult(input: Input, output: Output): ResultWrapper
}

object ResultCreator {

  def noContent[Input, Output](successStatus: Int = Status.NO_CONTENT): ResultCreator[Input, Output] =
    (_: Input, _: Output) => ResultWrapper(successStatus, None)

  def plainJson[Input, Output](successStatus: Int)(implicit ws: Writes[Output]): ResultCreator[Input, Output] =
    (_: Input, output: Output) => ResultWrapper(successStatus, Some(Json.toJson(output)))

}

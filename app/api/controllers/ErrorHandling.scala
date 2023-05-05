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

package api.controllers

import api.models.errors.ErrorWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Status

case class ErrorHandling(errorHandler: PartialFunction[ErrorWrapper, Result])

object ErrorHandling {

  val Default: ErrorHandling = ErrorHandling { case errorWrapper: ErrorWrapper =>
    Status(errorWrapper.error.httpStatus)(Json.toJson(errorWrapper))
  }

}

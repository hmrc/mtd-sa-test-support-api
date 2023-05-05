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

package api.controllers.requestParsers.validators

import api.models.errors.MtdError
import api.models.request.RawData

trait Validator[A <: RawData] {

  type ValidationLevel[T] = T => List[MtdError]

  def validate(data: A): List[MtdError]

  def run(validationSet: List[A => List[List[MtdError]]], data: A): List[MtdError] = {

    validationSet match {
      case Nil => List()
      case thisLevel :: remainingLevels =>
        thisLevel(data).flatten match {
          case x if x.isEmpty  => run(remainingLevels, data)
          case x if x.nonEmpty => x
        }
    }
  }

  protected def flattenErrors(errors: List[List[MtdError]]): List[MtdError] = {
    errors.flatten
      .groupBy(_.message)
      .map {
        case (_, errors) =>
          val baseError = errors.head.copy(paths = Some(Seq.empty[String]))

          errors.fold(baseError)(
            (error1, error2) => error1.copy(paths = Some(error1.paths.getOrElse(Seq.empty[String]) ++ error2.paths.getOrElse(Seq.empty[String])))
          )
      }
      .toList
  }

  protected def errorsResult(errors: Seq[MtdError]): Either[List[MtdError], Unit] =
    Either.cond(errors.isEmpty, Right(()), combine(errors))

  private def combine(errors: Seq[MtdError]): List[MtdError] =
    errors
      .groupBy(_.message)
      .foldLeft(List[MtdError]()) {
        case (acc, (_, errs)) =>
          val paths      = errs.flatMap(_.paths.getOrElse(Nil))
          val maybePaths = if (paths.isEmpty) None else Some(paths)

          acc ++ errs.headOption.map(_.copy(paths = maybePaths))
      }

}

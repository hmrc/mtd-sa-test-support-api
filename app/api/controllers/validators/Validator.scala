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

package api.controllers.validators

import api.models.errors.{BadRequestError, ErrorWrapper, MtdError}
import api.utils.Logging
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.*

trait Validator[+PARSED] extends Logging {

  def validate: Validated[Seq[MtdError], PARSED]

  def validateAndWrapResult()(implicit correlationId: String): Either[ErrorWrapper, PARSED] = {
    validate match {
      case Valid(parsed) =>
        logger.info(s"Validation successful for the request with CorrelationId: $correlationId")
        Right(parsed)

      case Invalid(errs) =>
        combineErrors(errs) match {
          case err :: Nil =>
            logger.warn(s"Validation failed with ${err.code} error for the request with CorrelationId: $correlationId")
            Left(ErrorWrapper(correlationId, err, None))

          case errs =>
            logger.warn(s"Validation failed with ${errs.map(_.code).mkString(",")} error for the request with CorrelationId: $correlationId")
            Left(ErrorWrapper(correlationId, BadRequestError, Some(errs)))
        }
    }
  }

  protected def invalid(error: MtdError): Invalid[Seq[MtdError]] = Invalid(List(error))

  protected def combine(results: Validated[Seq[MtdError], ?]*): Validated[Seq[MtdError], Unit] =
    results.traverse_(identity)

  private def combineErrors(errors: Seq[MtdError]): Seq[MtdError] = {
    errors
      .groupBy(_.message)
      .map { (_, errors) =>
        val baseError = errors.head.copy(paths = Some(Seq.empty[String]))

        errors.fold(baseError)((error1, error2) => {
          val paths: Option[Seq[String]] = for {
            error1Paths <- error1.paths
            error2Paths <- error2.paths
          } yield {
            error1Paths ++ error2Paths
          }
          error1.copy(paths = paths)
        })
      }
      .toList
      .sortBy(_.code)
  }

}

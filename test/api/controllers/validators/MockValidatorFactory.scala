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

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite

trait MockValidatorFactory[Request] extends TestSuite with MockFactory {

  def validator(): CallHandler[Validator[Request]]

  final def willUseValidator(use: Validator[Request]): CallHandler[Validator[Request]] =
    validator()
      .anyNumberOfTimes()
      .returns(use)

  final def returningSuccess(result: Request): Validator[Request] =
    new Validator[Request] {
      def validate: Validated[Seq[MtdError], Request] = Valid(result)
    }

  final def returning(result: MtdError*): Validator[Request] = returningErrors(result)

  final def returningErrors(result: Seq[MtdError]): Validator[Request] = new Validator[Request] {
    def validate: Validated[Seq[MtdError], Request] = Invalid(result)
  }

}

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

package uk.gov.hmrc.mtdsatestsupportapi.mocks.validators

import api.models.errors.MtdError
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.CreateAmendITSAStatusValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRawData

trait MockCreateAmendITSAStatusValidator extends TestSuite with MockFactory {

  val mockValidator: CreateAmendITSAStatusValidator = mock[CreateAmendITSAStatusValidator]

  object MockCreateAmendITSAStatusValidator {

    def validate(data: CreateAmendITSAStatusRawData): CallHandler[List[MtdError]] = {
      (mockValidator
        .validate(_: CreateAmendITSAStatusRawData))
        .expects(data)
    }

  }

}

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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{NinoValidation, NoValidationErrors}
import api.models.errors.MtdError
import uk.gov.hmrc.mtdsatestsupportapi.models.request.listCheckpoints.ListCheckpointsRawData

import javax.inject.Singleton

@Singleton
class ListCheckpointsValidator extends Validator[ListCheckpointsRawData] {

  override def validate(data: ListCheckpointsRawData): List[MtdError] = {
    data.nino match {
      case Some(nino) => NinoValidation.validate(nino)
      case None       => NoValidationErrors
    }
  }

}

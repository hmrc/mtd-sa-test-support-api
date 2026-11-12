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

package v1.createCheckpoint

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolveNino
import api.models.errors.{MtdError, NinoFormatError}
import cats.data.Validated
import cats.data.Validated.Invalid
import v1.createCheckpoint.model.request.CreateCheckpointRequestData

import javax.inject.Inject

class CreateCheckpointValidator @Inject() (vendorClientId: String, nino: Option[String]) extends Validator[CreateCheckpointRequestData] {

  def validate: Validated[Seq[MtdError], CreateCheckpointRequestData] =
    nino.fold(Invalid(List(NinoFormatError))) { nino =>
      ResolveNino(nino).map(validNino => CreateCheckpointRequestData(vendorClientId, validNino))
    }

}

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

package api.controllers.resolvers

import api.models.domain.Nino
import api.models.errors.{MtdError, NinoFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object ResolveNino {

  def apply(value: String): Validated[Seq[MtdError], Nino] =
    if (isValid(value))
      Valid(Nino(value))
    else
      Invalid(List(NinoFormatError))

  def isValid(nino: String): Boolean = nino != null && hasValidPrefix(nino) && ninoRegex.matches(nino)

  private def hasValidPrefix(nino: String) = !invalidPrefixes.exists(nino.startsWith)

  private val ninoRegex =
    ("^([ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|" +
      "[KT][A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y])[0-9]{6}[A-D ]?$").r

  private val invalidPrefixes =
    List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")

}

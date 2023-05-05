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

package api.connectors.httpparsers

import play.api.http.Status.{ FORBIDDEN, OK, UNAUTHORIZED }
import play.api.libs.json._
import uk.gov.hmrc.http.{ HttpReads, HttpResponse }
import api.connectors.MtdIdLookupOutcome
import api.models.errors.{ InternalError, InvalidBearerTokenError, NinoFormatError }

object MtdIdLookupHttpParser extends HttpParser {

  private val mtdIdJsonReads: Reads[String] = (__ \ "mtdbsa").read[String]

  implicit val mtdIdLookupHttpReads: HttpReads[MtdIdLookupOutcome] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK =>
        response.validateJson[String](mtdIdJsonReads) match {
          case Some(mtdId) => Right(mtdId)
          case None        => Left(InternalError)
        }
      case FORBIDDEN    => Left(NinoFormatError)
      case UNAUTHORIZED => Left(InvalidBearerTokenError)
      case _            => Left(InternalError)
    }
  }
}

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

package uk.gov.hmrc.internaltestsupport.utils

import api.models.errors.MtdError
import uk.gov.hmrc.internaltestsupport.models.OAuthCodeRetrievalError

import java.net.{URI, URLDecoder}
import scala.util.Try
import scala.util.matching.Regex

object UrlParser {

  private val CodeRegex: Regex = "code=([^&<\\s]+)".r

  def extractCode(url: String): Either[MtdError, String] = {

    def decode(s: String): String =
      URLDecoder.decode(s, "UTF-8")

    def extractFrom(component: String): Option[String] =
      Option(component)
        .filter(_.nonEmpty)
        .flatMap { str =>
          str.split("&amp;").iterator.collectFirst {
            case part if part.startsWith("code=") =>
              decode(part.substring("code=".length))
          }
        }

    def fromUri(u: String): Option[String] =
      Try(new URI(u)).toOption.flatMap { uri =>
        extractFrom(uri.getQuery).orElse(extractFrom(uri.getFragment))
      }

    def fromRegex(u: String): Option[String] =
      CodeRegex.findFirstMatchIn(u).map(m => decode(m.group(1)))

    fromUri(url)
      .orElse(fromRegex(url))
      .toRight(OAuthCodeRetrievalError)
  }

}

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

import java.net.URLDecoder
import scala.util.control.NonFatal

object UrlParser {

  def extractCode(url: String): String = {
    try {
      try {
        val uri      = new java.net.URI(url)
        val queryOpt = Option(uri.getQuery)
        val fragOpt  = Option(uri.getFragment)

        def extractFrom(s: String): Option[String] = {
          if (s == null || s.isEmpty) None
          else {
            s.split("&").toList.collectFirst {
              case part if part.startsWith("code=") =>
                URLDecoder.decode(part.substring(5), "UTF-8")
            }
          }
        }

        val codeOpt = queryOpt.flatMap(extractFrom).orElse(fragOpt.flatMap(extractFrom))

        codeOpt.getOrElse {
          val CodeRegex = "code=([^&<\\s]+)".r
          CodeRegex
            .findFirstMatchIn(url)
            .map(_.group(1))
            .getOrElse(throw new RuntimeException(s"No OAuth code found in URL: $url"))
        }
      } catch {
        case _: java.net.URISyntaxException =>
          val CodeRegex = "code=([^&<\\s]+)".r
          CodeRegex
            .findFirstMatchIn(url)
            .map(_.group(1))
            .getOrElse(throw new RuntimeException(s"No OAuth code found in URL: $url"))
      }
    } catch {
      case NonFatal(e) => throw e
    }
  }

}

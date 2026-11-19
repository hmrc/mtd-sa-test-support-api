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

package api.utils

import java.net.URI

object UrlUtils {

  def appendQueryParams(uri: String, queryParams: Seq[(String, String)]): String =
    if (queryParams.isEmpty) {
      uri
    } else {
      val oldUri   = new URI(uri)
      val oldQuery = oldUri.getQuery

      val appendQuery = queryParams.map { (k, v) => s"$k=$v" }.mkString("&")

      val newQuery = Option(oldQuery) match {
        case None                => appendQuery
        case Some(existingQuery) => s"$existingQuery&$appendQuery"
      }

      new URI(oldUri.getScheme, oldUri.getAuthority, oldUri.getPath, newQuery, oldUri.getFragment).toString
    }

}

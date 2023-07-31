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

package api.hateoas

import play.api.libs.json.{JsObject, Json, OWrites, Writes}

object HateoasWrapper {

  implicit def writesEmpty: Writes[HateoasWrapper[Unit]] = Writes { w =>
    if (w.links.nonEmpty) {
      Json.obj("links" -> Json.toJson(w.links))
    } else {
      Json.obj()
    }
  }

  implicit def writes[A: OWrites]: Writes[HateoasWrapper[A]] = Writes { w =>
    // Explicitly use writes method rather than Json.toJson so that we don't have to
    // throw out meaningless JsArray, JsString, etc cases...
    implicitly[OWrites[A]].writes(w.payload) match {
      case payloadJson: JsObject =>
        if (w.links.nonEmpty) {
          // Manually construct JsObject circumventing `.+` operator to preserve order of fields
          JsObject(payloadJson.fields :+ "links" -> Json.toJson(w.links))
        } else {
          payloadJson
        }
    }
  }

}

case class HateoasWrapper[A](payload: A, links: Seq[Link])

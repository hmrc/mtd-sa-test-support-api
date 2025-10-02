/*
 * Copyright 2025 HM Revenue & Customs
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

package utils.enums

import cats.Show
import play.api.libs.json.*

import scala.reflect.ClassTag

object Shows {
  given toStringShow[E]: Show[E] = Show.show(_.toString)
}

object Enums {
  private def typeName[E: ClassTag]: String = summon[ClassTag[E]].runtimeClass.getSimpleName

  def parser[E](values: Array[E])(using ev: Show[E] = Shows.toStringShow[E]): PartialFunction[String, E] =
    values.map(e => ev.show(e) -> e).toMap

  def reads[E: ClassTag](values: Array[E])(using ev: Show[E] = Shows.toStringShow[E]): Reads[E] =
    summon[Reads[String]].collect(JsonValidationError(s"error.expected.$typeName"))(parser(values))

  def writes[E](using ev: Show[E] = Shows.toStringShow[E]): Writes[E] = Writes[E](e => JsString(ev.show(e)))

  def format[E: ClassTag](values: Array[E])(using ev: Show[E] = Shows.toStringShow[E]): Format[E] =
    Format(reads(values), writes)

}

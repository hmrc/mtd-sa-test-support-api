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

package api.controllers.validators.resolvers

import api.controllers.validators.resolvers.UnexpectedJsonFieldsValidator.SchemaStructureSource
import api.models.errors.MtdError
import api.utils.Logging
import cats.data.Validated
import play.api.libs.json.*

class ResolveJsonObject[T](implicit val reads: Reads[T]) extends ResolverSupport with Logging {

  val resolver: Resolver[JsValue, T] = ResolveJsonObject.resolver

  def apply(data: JsValue): Validated[Seq[MtdError], T] = resolver(data)
}

object ResolveJsonObject extends ResolverSupport {

  def resolver[A: Reads]: Resolver[JsValue, A] = ResolveJsonObjectInternal.resolver.map(_._2)

  /** Gets a resolver that also validates for unexpected JSON fields
    */
  def strictResolver[A: Reads: SchemaStructureSource]: Resolver[JsValue, A] =
    ResolveJsonObjectInternal.resolver.thenValidate(UnexpectedJsonFieldsValidator.validator).map(_._2)

}

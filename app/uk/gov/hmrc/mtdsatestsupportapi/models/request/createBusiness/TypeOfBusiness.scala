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

package uk.gov.hmrc.mtdsatestsupportapi.models.request.createBusiness

import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait TypeOfBusiness

object TypeOfBusiness {
  case object `self-employment`      extends TypeOfBusiness
  case object `uk-property`          extends TypeOfBusiness
  case object `foreign-property`     extends TypeOfBusiness
  case object `property-unspecified` extends TypeOfBusiness

  def isProperty(typeOfBusiness: TypeOfBusiness): Boolean = typeOfBusiness match {
    case TypeOfBusiness.`self-employment` => false
    case _                                => true
  }

  implicit val reads: Reads[TypeOfBusiness]           = Enums.reads[TypeOfBusiness]
  val parser: PartialFunction[String, TypeOfBusiness] = Enums.parser[TypeOfBusiness]
}

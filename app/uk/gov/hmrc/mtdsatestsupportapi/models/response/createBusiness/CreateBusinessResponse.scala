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

package uk.gov.hmrc.mtdsatestsupportapi.models.response.createBusiness

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.Nino
import config.AppConfig
import play.api.libs.json._
import uk.gov.hmrc.mtdsatestsupportapi.models.hateoas.BusinessHateoasLinks

case class CreateBusinessResponse(businessId: String)

object CreateBusinessResponse extends BusinessHateoasLinks {
  implicit val reads: Reads[CreateBusinessResponse] = (__ \ "incomeSourceId").read[String].map(CreateBusinessResponse.apply)

  implicit val writes: OWrites[CreateBusinessResponse] = Json.writes

  implicit object LinksFactory extends HateoasLinksFactory[CreateBusinessResponse, CreateBusinessHateoasData] {

    override def links(appConfig: AppConfig, data: CreateBusinessHateoasData): Seq[Link] = {
      import data._
      Seq(deleteBusiness(appConfig, nino, businessId), listAllBusinesses(appConfig, nino), retrieveBusinessDetails(appConfig, nino, businessId))
    }

  }

}

case class CreateBusinessHateoasData(nino: Nino, businessId: String) extends HateoasData

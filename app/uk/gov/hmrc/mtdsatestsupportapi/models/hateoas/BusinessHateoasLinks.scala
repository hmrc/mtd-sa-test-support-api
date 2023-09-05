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

package uk.gov.hmrc.mtdsatestsupportapi.models.hateoas

import api.hateoas.Link
import api.hateoas.Method.{DELETE, GET, POST}
import api.models.domain.Nino
import config.AppConfig

trait BusinessHateoasLinks {

  val SELF = "self"

  private def baseUrl(appConfig: AppConfig) =
    s"/${appConfig.apiGatewayContext}/business"

  private def businessDetailsBaseUrl(appConfig: AppConfig) =
    s"/${appConfig.businessDetailsApiGatewayContext}"

  def createBusiness(appConfig: AppConfig, nino: Nino): Link =
    Link(href = s"${baseUrl(appConfig)}/$nino", method = POST, rel = "create-business")

  def deleteBusiness(appConfig: AppConfig, nino: Nino, businessId: String): Link =
    Link(href = s"${baseUrl(appConfig)}/$nino/$businessId", method = DELETE, rel = "delete-business")

  def listAllBusinesses(appConfig: AppConfig, nino: Nino): Link =
    Link(href = s"${businessDetailsBaseUrl(appConfig)}/$nino/list", method = GET, rel = "list-businesses")

  def retrieveBusinessDetails(appConfig: AppConfig, nino: Nino, businessId: String): Link =
    Link(href = s"${businessDetailsBaseUrl(appConfig)}/$nino/$businessId", method = GET, rel = "retrieve-business-details")

}

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

package uk.gov.hmrc.internaltestsupport.services

import api.controllers.RequestContext
import api.models.errors.*
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import uk.gov.hmrc.internaltestsupport.connectors.OAuthConnector
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OAuthService @Inject() (connector: OAuthConnector) extends BaseService {

  def getOAuthToken(request: OAuthRequest)(implicit ec: ExecutionContext, rc: RequestContext): Future[ServiceOutcome[OAuthResponse]] = {
    connector.getOAuthToken(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] = {
    Map(
      "SERVER_ERROR" -> InternalError
    )
  }

}

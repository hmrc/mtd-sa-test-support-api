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

package uk.gov.hmrc.internaltestsupport.connectors

import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.connectors.httpparsers.StandardDownstreamHttpParser.SuccessCode
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.AppConfig
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.internaltestsupport.models.oauth.{OAuthRequest, OAuthResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OAuthConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector with StandardDownstreamHttpParser {

  def getOauthToken(
      request: OAuthRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[OAuthResponse]] = {

    implicit val successCode: SuccessCode  = SuccessCode(OK)
    implicit val context: ConnectorContext = ConnectorContext(appConfig.oauthDownstreamConfig)

    post(request, DownstreamUri[OAuthResponse](s"oauth/token"))
  }

}

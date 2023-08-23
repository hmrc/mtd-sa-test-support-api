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

package uk.gov.hmrc.mtdsatestsupportapi.connectors

import api.connectors.{DownstreamOutcome, _}
import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.connectors.httpparsers.StandardDownstreamHttpParser.SuccessCode
import config.AppConfig
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mtdsatestsupportapi.models.request.restoreCheckpoint.RestoreCheckpointRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RestoreCheckpointConnector @Inject() (val appConfig: AppConfig, val http: HttpClientV2)
    extends BaseDownstreamConnector
    with StandardDownstreamHttpParser {

  def restoreCheckpoint(
      request: RestoreCheckpointRequest)(implicit ec: ExecutionContext, hc: HeaderCarrier, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    implicit val successCode: SuccessCode  = SuccessCode(CREATED)
    implicit val context: ConnectorContext = ConnectorContext(appConfig.stubDownstreamConfig)

    val downstreamPath = s"/test-support/vendor-state/$vendorClientId/checkpoints/${checkpointId.value}/restore"

    postEmpty(DownstreamUri[Unit](downstreamPath))
  }

}

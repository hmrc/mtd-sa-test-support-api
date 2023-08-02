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

import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.connectors.httpparsers.StandardDownstreamHttpParser.SuccessCode
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.AppConfig
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createCheckpoint.CreateCheckpointRequest
import uk.gov.hmrc.mtdsatestsupportapi.models.response.createCheckpoint.CreateCheckpointResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateCheckpointConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig)
    extends BaseDownstreamConnector
    with StandardDownstreamHttpParser {

  def createCheckpoint(request: CreateCheckpointRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateCheckpointResponse]] = {
    import request._

    implicit val successCode: SuccessCode  = SuccessCode(CREATED)
    implicit val context: ConnectorContext = ConnectorContext(appConfig.stubDownstreamConfig)

    val path = s"test-support/vendor-state/$vendorClientId/checkpoints?taxableEntityId=${nino.value}"

    postEmpty(DownstreamUri[CreateCheckpointResponse](path))
  }

}

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

import api.connectors.DownstreamUri
import api.connectors.httpparsers.StandardDownstreamHttpParser
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.DeleteStatefulTestDataRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteVendorStateConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig)
    extends BaseDownstreamConnector
    with StandardDownstreamHttpParser {

  def deleteVendorState(request: DeleteStatefulTestDataRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {
    import request._

    val downstreamConfig = appConfig.stubDownstreamConfig

    val url = nino match {
      case Some(n) => url"${downstreamConfig.baseUrl}/test-support/vendor-state/$vendorClientId?taxableEntityId=${n.value}"
      case None    => url"${downstreamConfig.baseUrl}/test-support/vendor-state/$vendorClientId"
    }

    delete(downstreamConfig)(DownstreamUri[Unit](url))
  }

}

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

package api.connectors

import config.{AppConfig, DownstreamConfig}
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector extends Logging {
  val http: HttpClientV2
  val appConfig: AppConfig

  private val jsonContentTypeHeader = HeaderNames.CONTENT_TYPE -> MimeTypes.JSON

  case class ConnectorContext(downstreamConfig: DownstreamConfig)(implicit
      val ec: ExecutionContext,
      val hc: HeaderCarrier,
      val correlationId: String) {
    def baseUrl: String = downstreamConfig.baseUrl
  }

  def post[Body: Writes, Resp](body: Body, downstreamUri: DownstreamUri[Resp])(implicit
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      connectorContext: ConnectorContext): Future[DownstreamOutcome[Resp]] = {

    implicit val hc: HeaderCarrier    = downstreamHeaderCarrier(jsonContentTypeHeader)
    implicit val ec: ExecutionContext = connectorContext.ec

    http.post(downstreamUri.value).withBody(Json.toJson(body)).execute
  }

  def get[Resp](downstreamUri: DownstreamUri[Resp])(implicit
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      connectorContext: ConnectorContext): Future[DownstreamOutcome[Resp]] = {

    implicit val hc: HeaderCarrier    = downstreamHeaderCarrier()
    implicit val ec: ExecutionContext = connectorContext.ec

    http.get(downstreamUri.value).execute
  }

  def put[Body: Writes, Resp](body: Body, downstreamUri: DownstreamUri[Resp])(implicit
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      connectorContext: ConnectorContext): Future[DownstreamOutcome[Resp]] = {

    implicit val hc: HeaderCarrier    = downstreamHeaderCarrier(jsonContentTypeHeader)
    implicit val ec: ExecutionContext = connectorContext.ec

    http.put(downstreamUri.value).withBody(Json.toJson(body)).execute
  }

  def delete[Resp](downstreamUri: DownstreamUri[Resp])(implicit
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      connectorContext: ConnectorContext): Future[DownstreamOutcome[Resp]] = {

    implicit val hc: HeaderCarrier    = downstreamHeaderCarrier()
    implicit val ec: ExecutionContext = connectorContext.ec

    http.delete(downstreamUri.value).execute
  }

  private def downstreamHeaderCarrier(additionalHeaders: (String, String)*)(implicit connectorContext: ConnectorContext): HeaderCarrier = {
    val passThroughHeaders = connectorContext.hc
      .headers(connectorContext.downstreamConfig.environmentHeaders.getOrElse(Nil))
      .filterNot(hdr => additionalHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

    HeaderCarrier(
      extraHeaders = connectorContext.hc.extraHeaders ++
        // Contract headers
        List(
          "CorrelationId" -> connectorContext.correlationId
        ) ++
        additionalHeaders ++
        passThroughHeaders
    )
  }

}

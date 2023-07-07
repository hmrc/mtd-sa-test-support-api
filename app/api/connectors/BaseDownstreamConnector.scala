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

  def post[Body: Writes, Resp](downstreamConfig: DownstreamConfig)(body: Body, downstreamUri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.post(downstreamUri.value).withBody(Json.toJson(body)).execute
    }

    doPost(getBackendHeaders(downstreamConfig)(hc, correlationId, jsonContentTypeHeader))
  }

  def get[Resp](downstreamConfig: DownstreamConfig)(downstreamUri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.get(downstreamUri.value).execute
    }

    doGet(getBackendHeaders(downstreamConfig)(hc, correlationId))
  }

  def put[Body: Writes, Resp](downstreamConfig: DownstreamConfig)(body: Body, downstreamUri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.put(downstreamUri.value).withBody(Json.toJson(body)).execute
    }

    doPut(getBackendHeaders(downstreamConfig)(hc, correlationId, jsonContentTypeHeader))
  }

  def delete[Resp](downstreamConfig: DownstreamConfig)(downstreamUri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.delete(downstreamUri.value).execute
    }

    doDelete(getBackendHeaders(downstreamConfig)(hc, correlationId))
  }

  private def getBackendHeaders(
      downstreamConfig: DownstreamConfig)(hc: HeaderCarrier, correlationId: String, additionalHeaders: (String, String)*): HeaderCarrier = {
    val passThroughHeaders = hc
      .headers(downstreamConfig.environmentHeaders.getOrElse(Nil))
      .filterNot(hdr => additionalHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        List(
          "CorrelationId" -> correlationId
        ) ++
        additionalHeaders ++
        passThroughHeaders
    )
  }
}

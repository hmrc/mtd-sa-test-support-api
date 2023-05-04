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

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import config.{AppConfig, FeatureSwitches}
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging
import utils.UrlUtils.appendQueryParams

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector extends Logging {
  val http: HttpClient
  val appConfig: AppConfig

  private val jsonContentTypeHeader = HeaderNames.CONTENT_TYPE -> MimeTypes.JSON

  implicit protected lazy val featureSwitches: FeatureSwitches = FeatureSwitches(appConfig.featureSwitches)

  def post[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.POST(getBackendUri(uri), body)
    }

    doPost(getBackendHeaders(uri, hc, correlationId, jsonContentTypeHeader))
  }

  def get[Resp](uri: DownstreamUri[Resp], queryParams: Seq[(String, String)] = Nil)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.GET(getBackendUri(uri), queryParams)
    }

    doGet(getBackendHeaders(uri, hc, correlationId))
  }

  def put[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.PUT(getBackendUri(uri), body)
    }

    doPut(getBackendHeaders(uri, hc, correlationId, jsonContentTypeHeader))
  }

  def delete[Resp](uri: DownstreamUri[Resp], queryParams: Seq[(String, String)] = Nil)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      // http.DELETE doesn't accept query params (unlike http.GET), so need to construct the query here:
      val downstreamUri = appendQueryParams(getBackendUri(uri), queryParams)
      http.DELETE(downstreamUri)
    }

    doDelete(getBackendHeaders(uri, hc, correlationId))
  }

  private def getBackendUri[Resp](uri: DownstreamUri[Resp]): String =
    s"${configFor(uri).baseUrl}/${uri.value}"

  private def getBackendHeaders[Resp](uri: DownstreamUri[Resp],
                                      hc: HeaderCarrier,
                                      correlationId: String,
                                      additionalHeaders: (String, String)*): HeaderCarrier = {
    val downstreamConfig = configFor(uri)

    val passThroughHeaders = hc
      .headers(downstreamConfig.environmentHeaders.getOrElse(Nil))
      .filterNot(hdr => additionalHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        List(
          "Authorization" -> s"Bearer ${downstreamConfig.token}",
          "Environment"   -> downstreamConfig.env,
          "CorrelationId" -> correlationId
        ) ++
        additionalHeaders ++
        passThroughHeaders
    )
  }

  private def configFor[Resp](uri: DownstreamUri[Resp]) =
    uri match {
      case DesUri(_)                => appConfig.desDownstreamConfig
      case IfsUri(_)                => appConfig.ifsDownstreamConfig
      case TaxYearSpecificIfsUri(_) => appConfig.taxYearSpecificIfsDownstreamConfig
    }

}

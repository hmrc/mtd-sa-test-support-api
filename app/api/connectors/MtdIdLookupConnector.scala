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

import config.AppConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object MtdIdLookupConnector {
  case class Error(statusCode: Int) extends AnyVal

  type Outcome = Either[MtdIdLookupConnector.Error, String]

}

@Singleton
class MtdIdLookupConnector @Inject() (http: HttpClientV2, appConfig: AppConfig) {

  def getMtdId(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MtdIdLookupConnector.Outcome] = {
    import api.connectors.httpparsers.MtdIdLookupHttpParser.mtdIdLookupHttpReads

    http.get(url"${appConfig.mtdIdBaseUrl}/mtd-identifier-lookup/nino/$nino").execute[MtdIdLookupConnector.Outcome]
  }

}

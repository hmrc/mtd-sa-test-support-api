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

package api.connectors.httpparsers

import api.models.errors.*
import api.utils.Logging
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Failure, Success, Try}

trait HttpParser extends Logging {

  implicit class JsonResponseHelper(response: HttpResponse) {
    private lazy val jsonTry: Try[JsValue] = Try(response.json)

    def validateJson[T](implicit reads: Reads[T]): Option[T] = jsonTry.toOption.flatMap(_.asOpt[T])

    def validateJsonWithLogging[T](implicit reads: Reads[T]): Option[T] =
      jsonTry match {
        case Success(json) =>
          json
            .validate[T]
            .fold(
              errors => {
                logger.warn(s"[JsonResponseHelper][validateJsonWithLogging] JSON validation failed: $errors")
                None
              },
              value => Some(value)
            )
        case Failure(_) =>
          logger.warn("[JsonResponseHelper][validateJsonWithLogging] Response body is not valid JSON")
          None
      }

  }

  def retrieveCorrelationId(response: HttpResponse): String = response.header("CorrelationId").getOrElse("")

  private val multipleErrorReads: Reads[List[DownstreamErrorCode]] = (__ \ "failures").read[List[DownstreamErrorCode]]

  private val bvrErrorReads: Reads[Seq[DownstreamErrorCode]] = {
    implicit val errorIdReads: Reads[DownstreamErrorCode] = (__ \ "id").read[String].map(DownstreamErrorCode(_))
    (__ \ "bvrfailureResponseElement" \ "validationRuleFailures").read[Seq[DownstreamErrorCode]]
  }

  def parseErrors(response: HttpResponse): DownstreamError = {
    val wrappedResponse: JsonResponseHelper = new JsonResponseHelper(response)

    lazy val singleError    = wrappedResponse.validateJson[DownstreamErrorCode].map(err => DownstreamErrors(List(err)))
    lazy val multipleErrors = wrappedResponse.validateJson(multipleErrorReads).map(errs => DownstreamErrors(errs))
    lazy val bvrErrors      = wrappedResponse.validateJson(bvrErrorReads).map(errs => OutboundError(BVRError, Some(errs.map(_.toMtd(BAD_REQUEST)))))
    lazy val unableToParseJsonError = {
      logger.warn(s"unable to parse errors from response: ${response.body}")
      OutboundError(InternalError)
    }

    singleError orElse multipleErrors orElse bvrErrors getOrElse unableToParseJsonError
  }

}

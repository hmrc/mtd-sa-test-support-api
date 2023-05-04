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

package api.support

import api.controllers.EndpointLogContext
import api.models.errors.{
  BadRequestError,
  DownstreamError,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  OutboundError,
  RuleIncorrectGovTestScenarioError
}
import api.models.outcomes.ResponseWrapper
import utils.Logging

trait DownstreamResponseMappingSupport {
  self: Logging =>

  final def mapDownstreamErrors[D](errorCodeMap: PartialFunction[String, MtdError])(downstreamResponseWrapper: ResponseWrapper[DownstreamError])(
      implicit logContext: EndpointLogContext): ErrorWrapper = {

    lazy val defaultErrorCodeMapping: String => MtdError = {
      case "UNMATCHED_STUB_ERROR" => {
        logger.warn(s"[${logContext.controllerName}] [${logContext.endpointName}] - No matching stub was found")
        RuleIncorrectGovTestScenarioError
      }
      case code => {
        logger.warn(s"[${logContext.controllerName}] [${logContext.endpointName}] - No mapping found for error code $code")
        InternalError
      }
    }

    downstreamResponseWrapper match {
      case ResponseWrapper(correlationId, DownstreamErrors(error :: Nil)) =>
        ErrorWrapper(correlationId, errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping), None)

      case ResponseWrapper(correlationId, DownstreamErrors(errorCodes)) =>
        val mtdErrors = errorCodes.map(error => errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping))

        if (mtdErrors.contains(InternalError)) {
          logger.warn(
            s"[${logContext.controllerName}] [${logContext.endpointName}] [CorrelationId - $correlationId]" +
              s" - downstream returned ${errorCodes.map(_.code).mkString(",")}. Revert to ISE")
          ErrorWrapper(correlationId, InternalError, None)
        } else {
          ErrorWrapper(correlationId, BadRequestError, Some(mtdErrors))
        }

      case ResponseWrapper(correlationId, OutboundError(error, errors)) =>
        ErrorWrapper(correlationId, error, errors)
    }
  }

}

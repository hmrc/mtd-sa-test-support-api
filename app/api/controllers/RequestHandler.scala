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

package api.controllers

import api.controllers.validators.Validator
import api.models.errors.{ErrorWrapper, InternalError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import api.utils.Logging
import cats.data.EitherT
import cats.implicits.*
import play.api.http.Status
import play.api.libs.json.Writes
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError

import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler {
  def handleRequest()(implicit ctx: RequestContext, ec: ExecutionContext): Future[Result]
}

object RequestHandler {

  def withValidator[Input](validator: Validator[Input]): ValidatorOnlyBuilder[Input] =
    new ValidatorOnlyBuilder[Input](validator)

  class ValidatorOnlyBuilder[Input] private[RequestHandler] (validator: Validator[Input]) {

    def withService[Output](serviceFunction: Input => Future[ServiceOutcome[Output]]): RequestHandlerBuilder[Input, Output] =
      RequestHandlerBuilder(validator, serviceFunction)

  }

  case class RequestHandlerBuilder[Input, Output] private[RequestHandler] (
      validator: Validator[Input],
      service: Input => Future[ServiceOutcome[Output]],
      errorHandling: ErrorHandling = ErrorHandling.Default,
      resultCreator: ResultCreator[Input, Output] = ResultCreator.noContent[Input, Output]()
  ) extends RequestHandler {

    def handleRequest()(implicit ctx: RequestContext, ec: ExecutionContext): Future[Result] =
      Delegate.handleRequest()

    def withErrorHandling(errorHandling: ErrorHandling): RequestHandlerBuilder[Input, Output] =
      copy(errorHandling = errorHandling)

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.plainJson(successStatus))
      * }}}
      */
    def withPlainJsonResult(successStatus: Int = Status.OK)(implicit ws: Writes[Output]): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.plainJson(successStatus))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.noContent)
      * }}}
      */
    def withNoContentResult(successStatus: Int = Status.NO_CONTENT): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.noContent(successStatus))

    private def withResultCreator(resultCreator: ResultCreator[Input, Output]): RequestHandlerBuilder[Input, Output] =
      copy(resultCreator = resultCreator)

    // Scoped as a private delegate so as to keep the logic completely separate from the configuration
    private object Delegate extends RequestHandler with Logging with RequestContextImplicits {

      implicit class Response(result: Result) {

        def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

          val headers = responseHeaders ++ List(
            "X-CorrelationId"        -> correlationId,
            "X-Content-Type-Options" -> "nosniff"
          )

          result.copy(header = result.header.copy(headers = result.header.headers ++ headers))
        }

      }

      def handleRequest()(implicit
          ctx: RequestContext,
          ec: ExecutionContext
      ): Future[Result] = {

        logger.info(
          message = s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] " +
            s"with correlationId : ${ctx.correlationId}")

        val result = for {
          parsedRequest   <- EitherT.fromEither[Future](validator.validateAndWrapResult())
          serviceResponse <- EitherT(service(parsedRequest))
        } yield doWithContext(ctx.withCorrelationId(serviceResponse.correlationId)) { implicit ctx: RequestContext =>
          handleSuccess(parsedRequest, serviceResponse)
        }

        result.leftMap { errorWrapper =>
          doWithContext(ctx.withCorrelationId(errorWrapper.correlationId)) { implicit ctx: RequestContext =>
            handleFailure(errorWrapper)
          }
        }.merge
      }

      private def doWithContext[A](ctx: RequestContext)(f: RequestContext => A): A = f(ctx)

      private def handleSuccess(parsedRequest: Input, serviceResponse: ResponseWrapper[Output])(implicit ctx: RequestContext): Result = {
        logger.info(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${ctx.correlationId}")

        val resultWrapper = resultCreator
          .createResult(parsedRequest, serviceResponse.responseData)

        resultWrapper.asResult.withApiHeaders(ctx.correlationId)
      }

      private def handleFailure(errorWrapper: ErrorWrapper)(implicit ctx: RequestContext): Result = {
        logger.warn(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: ${ctx.correlationId}")

        val errorResult = errorHandling.errorHandler.applyOrElse(errorWrapper, unhandledError)

        errorResult.withApiHeaders(ctx.correlationId)
      }

      private def unhandledError(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
        logger.error(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Unhandled error: $errorWrapper")
        InternalServerError(InternalError.asJson)
      }

    }

  }

}

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

package controllers

import config.AppConfig
import play.api.http.{AcceptEncoding, HttpErrorHandler}
import play.api.mvc.{Action, AnyContent, RequestHeader, Result}
import play.utils.{InvalidUriEncodingException, Resources}

import java.io.{BufferedReader, InputStream, InputStreamReader}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Rewriter {
  def apply(path: String, filename: String, contents: String): String
}

@Singleton
class RewriteableAssets @Inject() (errorHandler: HttpErrorHandler, meta: AssetsMetadata, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends Assets(errorHandler, meta) {
  import meta._

  /** If no rewriters, Play's own static Assets.assetAt() will be called.
    * @param path
    *   e.g. "/public/api/conf/1.0"
    * @param filename
    *   e.g. "schemas/retrieve_other_expenses_response.json" or "employment_expenses_delete.yaml"
    */
  def rewriteableAt(path: String, filename: String, rewriters: Seq[Rewriter]): Action[AnyContent] = {
    if (rewriters.isEmpty)
      at(path, filename)
    else
      Action.async { implicit request =>
        assetAt(path, filename, rewriters)
      }
  }

  // Mostly copied from the private method in Assets:
  private def assetAt(path: String, filename: String, rewrites: Seq[Rewriter])(implicit
      request: RequestHeader
  ): Future[Result] = {
    val assetName: Option[String] = resourceNameAt(path, filename)
    val assetInfoFuture: Future[Option[(AssetInfo, AcceptEncoding)]] = assetName
      .map { name =>
        assetInfoForRequest(request, name)
      }
      .getOrElse(Future.successful(None))

    def notFound: Future[Result] =
      errorHandler.onClientError(request, NOT_FOUND, "Resource not found by Assets controller")

    val pendingResult: Future[Result] = assetInfoFuture.flatMap {
      case Some((assetInfo, acceptEncoding)) =>
        val connection = assetInfo.url(acceptEncoding).openConnection()
        // Make sure it's not a directory
        if (Resources.isUrlConnectionADirectory(connection)) {
          Resources.closeUrlConnection(connection)
          notFound
        } else
          Future {
            val stream    = connection.getInputStream
            val text      = inputStreamToString(stream)
            val rewritten = rewrites.foldLeft(text)((acc, op) => op(path, filename, acc))
            val result    = Ok(rewritten)
            asEncodedResult(result, acceptEncoding, assetInfo)
          }

      case None => notFound
    }

    pendingResult.recoverWith {
      case e: InvalidUriEncodingException =>
        errorHandler.onClientError(request, BAD_REQUEST, s"Invalid URI encoding for $filename at $path: " + e.getMessage)
      case NonFatal(e) =>
        // Add a bit more information to the exception for better error reporting later
        errorHandler.onServerError(
          request,
          new RuntimeException(s"Unexpected error while serving rewriteable $filename at $path: " + e.getMessage, e)
        )
    }
  }

  private def inputStreamToString(is: InputStream) = {
    val inputStreamReader = new InputStreamReader(is)
    val bufferedReader    = new BufferedReader(inputStreamReader)
    (Iterator continually bufferedReader.readLine takeWhile (_ != null)).mkString("\n")
  }

  private def asEncodedResult(response: Result, acceptEncoding: AcceptEncoding, assetInfo: AssetInfo): Result = {
    assetInfo
      .bestEncoding(acceptEncoding)
      .map(enc => response.withHeaders(VARY -> ACCEPT_ENCODING, CONTENT_ENCODING -> enc))
      .getOrElse(if (assetInfo.varyEncoding) response.withHeaders(VARY -> ACCEPT_ENCODING) else response)
  }

}

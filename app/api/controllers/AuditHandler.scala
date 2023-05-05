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

package api.controllers

import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.ErrorWrapper
import api.services.AuditService
import cats.syntax.either._
import play.api.libs.json.{JsValue, Writes}

import scala.concurrent.ExecutionContext

trait AuditHandler extends RequestContextImplicits {

  def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Unit

}

object AuditHandler {

  trait AuditDetailCreator[A] {
    def createAuditDetail(userDetails: UserDetails, requestBody: Option[JsValue], auditResponse: AuditResponse, versionNumber: String)(implicit ctx: RequestContext): A
  }

  def custom[A: Writes](auditService: AuditService,
                        auditType: String,
                        transactionName: String,
                        auditDetailCreator: AuditDetailCreator[A],
                        requestBody: Option[JsValue] = None,
                        includeResponse: Boolean = false): AuditHandler =
    new AuditHandlerImpl[A](
      auditService = auditService,
      auditType = auditType,
      transactionName = transactionName,
      auditDetailCreator,
      requestBody = requestBody,
      responseBodyMap = if (includeResponse) identity else _ => None
    )

  def apply(auditService: AuditService,
            auditType: String,
            transactionName: String,
            params: Map[String, String],
            requestBody: Option[JsValue] = None,
            includeResponse: Boolean = false): AuditHandler = {

    custom(
      auditService = auditService,
      auditType = auditType,
      transactionName = transactionName,
      auditDetailCreator = GenericAuditDetail.auditDetailCreator(params),
      requestBody = requestBody,
      includeResponse = includeResponse
    )
  }

  private class AuditHandlerImpl[A: Writes](auditService: AuditService,
                                            auditType: String,
                                            transactionName: String,
                                            auditDetailCreator: AuditDetailCreator[A],
                                            requestBody: Option[JsValue],
                                            responseBodyMap: Option[JsValue] => Option[JsValue])
      extends AuditHandler {

    def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(implicit
        ctx: RequestContext,
        ec: ExecutionContext): Unit = {

      val auditEvent = {
        val auditResponse = AuditResponse(httpStatus, response.map(responseBodyMap).leftMap(_.auditErrors))

        val detail = auditDetailCreator.createAuditDetail(
          userDetails = userDetails,
          requestBody = requestBody,
          auditResponse = auditResponse,
          versionNumber = versionNumber
        )

        AuditEvent(auditType, transactionName, detail)
      }

      auditService.auditEvent(auditEvent)
    }

  }

}

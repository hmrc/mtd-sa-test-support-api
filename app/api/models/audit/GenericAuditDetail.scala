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

package api.models.audit

import api.controllers.{AuditHandler, RequestContext}
import api.models.auth.UserDetails
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class GenericAuditDetail(versionNumber: String,
                              userType: String,
                              agentReferenceNumber: Option[String],
                              params: JsObject,
                              correlationId: String,
                              response: AuditResponse)

object GenericAuditDetail {

  implicit val writes: OWrites[GenericAuditDetail] = (
    (JsPath \ "versionNumber").write[String] and
      (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      JsPath.write[Map[String, JsValue]].contramap((p: JsObject) => p.value.toMap) and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[AuditResponse]
  )(unlift(GenericAuditDetail.unapply))

  def apply[A: OWrites](userDetails: UserDetails,
                        params: A,
                        correlationId: String,
                        response: AuditResponse,
                        versionNumber: String = "2.0"): GenericAuditDetail = {

    GenericAuditDetail(
      versionNumber = versionNumber,
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      params = Json.toJsObject(params),
      correlationId = correlationId,
      response = response
    )
  }

  def auditDetailCreator(params: Map[String, String]): AuditHandler.AuditDetailCreator[GenericAuditDetail] =
    new AuditHandler.AuditDetailCreator[GenericAuditDetail] {

      def createAuditDetail(userDetails: UserDetails, request: Option[JsValue], response: AuditResponse, versionNumber: String)(implicit
          ctx: RequestContext): GenericAuditDetail =
        GenericAuditDetail(
          versionNumber = versionNumber,
          userType = userDetails.userType,
          agentReferenceNumber = userDetails.agentReferenceNumber,
          params = Json.toJsObject(params),
          correlationId = ctx.correlationId,
          response = response
        )

    }

}

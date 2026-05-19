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

package uk.gov.hmrc.internaltestsupport.models

import play.api.libs.json.{Json, OFormat}

//TODO: Make the timeout config driven and remove the default value
case class FormSubmitRawRequest(nino: String, identifier: Option[String] = None, timeoutMs: Option[Int] = Some(15000))

object FormSubmitRawRequest {
  implicit val format: OFormat[FormSubmitRawRequest] = Json.format[FormSubmitRawRequest]
}

case class FormSubmitRequest(nino: String, identifier: String)

object FormSubmitRequest {

  def from(m: FormSubmitRawRequest, identifier: String): FormSubmitRequest = {
    FormSubmitRequest(m.nino, identifier)
  }

  implicit val format: OFormat[FormSubmitRequest] = Json.format[FormSubmitRequest]
}

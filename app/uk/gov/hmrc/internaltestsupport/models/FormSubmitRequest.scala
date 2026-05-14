package uk.gov.hmrc.internaltestsupport.models

import play.api.libs.json.{Json, OFormat}

//TODO: Make the timeout config driven and remove the default value
case class FormSubmitRawRequest(nino: String, identifier: Option[String] = None, timeoutMs: Option[Int] = Some(15000))

object FormSubmitRawRequest {
  implicit val format: OFormat[FormSubmitRequest] = Json.format[FormSubmitRequest]
}

case class FormSubmitRequest(nino: String, identifier: String)

object FormSubmitRequest {
  def from(m: FormSubmitRawRequest, identifier: String): Unit = {
    FormSubmitRequest(m.nino, identifier)
  }
  implicit val format: OFormat[FormSubmitRequest] = Json.format[FormSubmitRequest]
}

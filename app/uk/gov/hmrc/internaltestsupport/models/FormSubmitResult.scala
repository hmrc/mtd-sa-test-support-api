package uk.gov.hmrc.internaltestsupport.models

import play.api.libs.json.{Json, OFormat}

case class FormSubmitResult(oauthCode: Option[String] = None,
                            error: Option[String] = None)

object FormSubmitResult {
  implicit val format: OFormat[FormSubmitResult] = Json.format[FormSubmitResult]
}

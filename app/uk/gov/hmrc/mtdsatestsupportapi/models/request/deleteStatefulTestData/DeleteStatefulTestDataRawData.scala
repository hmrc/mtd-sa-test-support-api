package uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class DeleteStatefulTestDataRawData(vendorClientId: String, body: Option[JsValue])

object DeleteStatefulTestDataRawData {
  implicit val writes: Writes[DeleteStatefulTestDataRawData] = (
    (JsPath \ "vendorClientId").write[String] and
      (JsPath \ "body").writeNullable[JsValue]
  )(delete => (delete.vendorClientId, delete.body))
}
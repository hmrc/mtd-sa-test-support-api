package uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData

import play.api.libs.json.JsValue
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.VendorClientId

case class DeleteStatefulTestDataRequest(vendorClientId: VendorClientId, body: Option[JsValue])
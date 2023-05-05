package uk.gov.hmrc.mtdsatestsupportapi.models.domain

case class VendorClientId(id: String) {
  require(VendorClientId.isValid(id), s"$id is not a valid id.")
}

object VendorClientId {
  private def isValid(id: String): Boolean = id != null // TODO:  add any additional validation (e.g. regex) we chose to do
}

package api.controllers.requestParsers.validators.validations

import api.models.errors.VendorClientIdFormatError
import support.UnitSpec

class VendorClientIdValidationSpec extends UnitSpec {

  "VendorClientIdValidation when validating" should {
    "return no errors" when {
      "a valid vendor client id is supplied" in {
        val validVendorClientId = "some_id"

        VendorClientIdValidation.validate(validVendorClientId).isEmpty shouldBe true
      }
    }
    "return a VendorClientIdFormatError" when {
      "an invalid vendor client id format is supplied" in {
        val invalidVendorClientId = null

        VendorClientIdValidation.validate(invalidVendorClientId) shouldBe List(VendorClientIdFormatError)
      }
    }
  }

}

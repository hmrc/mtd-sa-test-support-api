package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.models.errors.StatusReasonFormat
import support.UnitSpec

class StatusReasonValidationSpec extends UnitSpec {

  "StatusReasonValidation" should {
    "return no error" when {
      checkValid("00")
      checkValid("01")
      checkValid("02")
      checkValid("03")
      checkValid("04")
      checkValid("05")
      checkValid("06")
      checkValid("07")
      checkValid("08")
      checkValid("09")

      def checkValid(value: String): Unit =
        s"provided with a string of '$value'" in {
          StatusReasonValidation.validate(value) shouldBe Nil
        }
    }
    "return an error" when {
      "invalid value is provided" in {
        StatusReasonValidation.validate("99") shouldBe List(StatusReasonFormat)
      }
    }
  }

}

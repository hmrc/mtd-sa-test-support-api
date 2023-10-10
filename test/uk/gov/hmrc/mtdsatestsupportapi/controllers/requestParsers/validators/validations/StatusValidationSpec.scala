package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.models.errors.StatusFormat
import support.UnitSpec

class StatusValidationSpec extends UnitSpec {

  "StatusValidation" should {
    "return no error" when {
      checkValid("00")
      checkValid("01")
      checkValid("02")
      checkValid("03")
      checkValid("04")
      checkValid("05")
      checkValid("99")

      def checkValid(value: String): Unit =
        s"provided with a string of '$value'" in {
          StatusValidation.validate(value) shouldBe Nil
        }
    }
    "return an error" when {
      "invalid value is provided" in {
        StatusValidation.validate("hello") shouldBe List(StatusFormat)
      }
    }
  }

}

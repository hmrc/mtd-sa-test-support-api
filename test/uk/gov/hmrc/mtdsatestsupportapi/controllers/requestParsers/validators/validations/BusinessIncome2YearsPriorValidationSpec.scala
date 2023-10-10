package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.validations

import api.models.errors.BusinessIncomePriorTo2YearsFormat
import support.UnitSpec

class BusinessIncome2YearsPriorValidationSpec extends UnitSpec {
  val error = BusinessIncomePriorTo2YearsFormat

  "BusinessIncome2YearsPriorValidation" must {
    "return no errors" when {
      "a valid value is provided" in {
        BusinessIncome2YearsPriorValidation.validateOptional(Some(2000), path = "/path") shouldBe Nil
      }
    }
    "return an error" when {
      "invalid value is provided" in {
        BusinessIncome2YearsPriorValidation.validateOptional(Some(-1000), path = "/path") shouldBe Seq(error)
      }
    }
  }

}

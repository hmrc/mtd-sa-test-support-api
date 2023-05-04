/*
 * Copyright 2023 HM Revenue & Customs
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

package api.controllers.requestParsers.validators.validations

import api.models.errors.RuleIncorrectOrEmptyBodyError
import api.utils.JsonErrorValidators
import play.api.libs.json.{Json, OFormat}
import shapeless.HNil
import support.UnitSpec
import utils.EmptinessChecker

class JsonFormatValidationSpec extends UnitSpec with JsonErrorValidators {

  case class TestDataObject(field1: String, field2: String, oneOf1: Option[String] = None, oneOf2: Option[String] = None)
  case class TestDataWrapper(arrayField: Seq[TestDataObject])

  implicit val testDataObjectFormat: OFormat[TestDataObject]   = Json.format[TestDataObject]
  implicit val testDataWrapperFormat: OFormat[TestDataWrapper] = Json.format[TestDataWrapper]

  // at least one of oneOf1 and oneOf2 must be included:
  implicit val emptinessChecker: EmptinessChecker[TestDataObject] = EmptinessChecker.use { o =>
    "oneOf1" -> o.oneOf1 :: "oneOf2" -> o.oneOf2 :: HNil
  }

  "validateOrRead" should {
    "return the object" when {
      "when a valid JSON object with all the necessary fields is supplied" in {

        val validJson = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse" }""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataObject](validJson)
        validationResult shouldBe Right(TestDataObject("Something", "SomethingElse"))
      }
    }

    "return an error " when {
      "required field is missing" in {
        val json = Json.parse("""{ "field1" : "Something" }""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataObject](json)
        validationResult shouldBe Left(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field2")))))
      }

      "required field is missing in array object" in {
        val json = Json.parse("""{ "arrayField" : [{ "field1" : "Something" }]}""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataWrapper](json)
        validationResult shouldBe Left(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/arrayField/0/field2")))))
      }

      "required field is missing in multiple array objects" in {
        val json = Json.parse("""{ "arrayField" : [{ "field1" : "Something" }, { "field1" : "Something" }]}""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataWrapper](json)
        validationResult shouldBe Left(
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq(
                "/arrayField/0/field2",
                "/arrayField/1/field2"
              )))))
      }

      "empty body is submitted" in {
        val json = Json.parse("""{}""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataObject](json)
        validationResult shouldBe Left(List(RuleIncorrectOrEmptyBodyError))
      }

      "a non-empty body is supplied without any expected fields" in {
        val json = Json.parse("""{"field": "value"}""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataObject](json)
        validationResult shouldBe Left(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field1", "/field2")))))
      }

      "a field is supplied with the wrong data type" in {
        val json = Json.parse("""{"field1": true, "field2": "value"}""")

        val validationResult = JsonFormatValidation.validateOrRead[TestDataObject](json)
        validationResult shouldBe Left(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field1")))))
      }
    }
  }

  "validateAndCheckNonEmpty" should {
    "validate against the json format" in {
      val json = Json.parse("""{ "field1" : "Something" }""")

      val validationResult = JsonFormatValidation.validate[TestDataObject](json)
      validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field2"))))
    }

    "detect empty objects" in {
      val validJson = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse" }""")

      val validationResult = JsonFormatValidation.validateAndCheckNonEmpty[TestDataObject](validJson)
      validationResult shouldBe List(RuleIncorrectOrEmptyBodyError)
    }

    "detect empty arrays" in {
      val json             = Json.parse("""{ "arrayField": [] }""")
      val validationResult = JsonFormatValidation.validateAndCheckNonEmpty[TestDataWrapper](json)

      validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/arrayField"))))
    }

    "return no error when all objects are non-empty" in {
      val validJson = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse", "oneOf1": "value" }""")

      val validationResult = JsonFormatValidation.validateAndCheckNonEmpty[TestDataObject](validJson)
      validationResult shouldBe Nil
    }
  }
}

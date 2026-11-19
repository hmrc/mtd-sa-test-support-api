/*
 * Copyright 2026 HM Revenue & Customs
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

package api.controllers.validators

import api.controllers.validators.resolvers.{ResolveJsonObject, ResolveNino, ResolveTaxYear}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.*
import org.scalamock.scalatest.MockFactory
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{JsValue, Json, Reads}

class ValidatorSpec extends UnitSpec with MockFactory {

  private implicit val correlationId: String = "1234"

  private val validNino    = Nino("AA123456A")
  private val validTaxYear = TaxYear.fromMtd("2023-24")

  private val validBody = Json.parse("""
    | {
    |   "value1": "value 1",
    |   "value2": true
    | }
    |""".stripMargin)

  private val parsedRequestBody = TestParsedRequestBody("value 1", value2 = true)
  private val parsedRequest     = TestParsedRequest(validNino, validTaxYear, parsedRequestBody)

  case class TestParsedRequest(nino: Nino, taxYear: TaxYear, body: TestParsedRequestBody)
  case class TestParsedRequestBody(value1: String, value2: Boolean)
  implicit val testParsedRequestBodyReads: Reads[TestParsedRequestBody] = Json.reads[TestParsedRequestBody]

  /** The main/outermost validator.
    */
  private class TestValidator(nino: String = "AA123456A", taxYear: String = "2023-24", jsonBody: JsValue = validBody)
      extends Validator[TestParsedRequest] {

    private val jsonResolver = new ResolveJsonObject[TestParsedRequestBody]

    def validate: Validated[Seq[MtdError], TestParsedRequest] =
      (
        ResolveNino(nino),
        ResolveTaxYear(taxYear),
        jsonResolver(jsonBody)
      ).mapN(TestParsedRequest.apply) andThen TestRulesValidator.validateBusinessRules

    override def invalid(error: MtdError): Invalid[Seq[MtdError]] = super.invalid(error)

    override def combine(results: Validated[Seq[MtdError], ?]*): Validated[Seq[MtdError], Unit] = super.combine(results*)
  }

  /** Perform additional business-rules validation on the correctly parsed request.
    */
  private object TestRulesValidator extends RulesValidator[TestParsedRequest] {

    def validateBusinessRules(parsed: TestParsedRequest): Validated[Seq[MtdError], TestParsedRequest] = {
      val resolvedValue1 = if (parsed.body.value1 == "value 1") valid else Invalid(List(RuleValue1Invalid))
      val resolvedValue2 = if (parsed.body.value2) valid else Invalid(List(RuleValue2Invalid))

      combine(
        resolvedValue1,
        resolvedValue2
      ).onSuccess(parsed)
    }

  }

  private object RuleValue1Invalid extends MtdError("RULE_VALUE_1_INVALID", "value1 can only be 'value 1'", BAD_REQUEST)
  private object RuleValue2Invalid extends MtdError("RULE_VALUE_2_INVALID", "value2 can only be true", BAD_REQUEST)

  "invalid(error)" should {
    "return the error wrapped in an Invalid(Seq)" in {
      val validator                      = new TestValidator()
      val result: Invalid[Seq[MtdError]] = validator.invalid(NinoFormatError)
      result shouldBe Invalid(List(NinoFormatError))
    }
  }

  "combine(results)" when {
    val validator = new TestValidator()

    "given Valid results" should {
      "return the results combined into a single Valid(Unit)" in {
        val result = validator.combine(
          Valid("any value"),
          Valid("any other value")
        )

        result shouldBe Valid(())
      }
    }

    "given some results including errors" should {
      "return the errors combined into a single Invalid that includes the MTD errors" in {
        val result = validator.combine(
          Valid("any value"),
          Valid("any other value"),
          Invalid(List(NinoFormatError)),
          Invalid(List(ValueFormatError))
        )

        result shouldBe Invalid(List(NinoFormatError, ValueFormatError))
      }
    }

  }

  "validateAndWrapResult()" should {

    "return the parsed domain object" when {
      "given valid input" in {
        val validator = new TestValidator()
        val result    = validator.validateAndWrapResult()
        result shouldBe Right(parsedRequest)
      }
    }

    "return an error from the request params" when {
      "given a single invalid request param" in {
        val validator = new TestValidator(nino = "not-a-nino")
        val result    = validator.validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given two invalid request params" in {
        val validator = new TestValidator(nino = "not-a-nino", taxYear = "not-a-tax-year")
        val result    = validator.validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }

    }

    "return an error from the Json body" when {
      "given a request with valid params and an invalid body" in {
        val jsonRequestBody = Json.parse("""
          | {
          |   "value1": "value 1",
          |   "value2": "not-a-boolean"
          | }
          |""".stripMargin)

        val validator = new TestValidator(jsonBody = jsonRequestBody)
        val result    = validator.validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/value2")))
      }
    }
  }

}

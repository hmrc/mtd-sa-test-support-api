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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, RuleIncorrectOrEmptyBodyError}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.mtdsatestsupportapi.mocks.validators.MockCreateAmendITSAStatusValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.domain.{Status, StatusReason}
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.{
  CreateAmendITSAStatusRawData,
  CreateAmendITSAStatusRequest,
  CreateAmendITSAStatusRequestBody,
  ITSAStatusDetail
}

class CreateAmendITSAStatusRequestParserSpec extends UnitSpec with MockCreateAmendITSAStatusValidator {

  private implicit val correlationId: String = "X-123"
  private val nino                           = "AA123456B"
  private val taxYear                        = "2023-24"

  val body: JsValue = Json.parse("""
      |{
      |  "itsaStatusDetails": [
      |    {
      |      "submittedOn": "2021-03-23T16:02:34.039Z",
      |      "status": "01",
      |      "statusReason": "02",
      |      "businessIncome2YearsPrior": 234
      |    }
      |  ]
      |}
      |""".stripMargin)

  val validBody = CreateAmendITSAStatusRequestBody(itsaStatusDetails = Seq(
    ITSAStatusDetail(
      submittedOn = "2021-03-23T16:02:34.039Z",
      status = Status.`01`,
      statusReason = StatusReason.`02`,
      businessIncome2YearsPrior = Some(234))))

  private val inputData = CreateAmendITSAStatusRawData(nino, taxYear, body)

  private val parser = new CreateAmendITSAStatusRequestParser(mockValidator)

  "CreateAmendITSAStatusRequestParser" should {
    "return a request object" when {
      "valid request data is provided" in {
        MockCreateAmendITSAStatusValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(CreateAmendITSAStatusRequest(Nino(nino), TaxYear.fromMtd(taxYear), validBody))
      }
    }
    "return an error" when {
      "a single validation error occurs" in {
        MockCreateAmendITSAStatusValidator.validate(inputData) returns List(NinoFormatError)

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in {
        MockCreateAmendITSAStatusValidator.validate(inputData) returns List(NinoFormatError, RuleIncorrectOrEmptyBodyError)

        parser.parseRequest(inputData) shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }

}

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

package api.controllers.requestParsers

import api.controllers.requestParsers.validators.Validator
import api.models.domain.Nino
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, RuleIncorrectOrEmptyBodyError}
import api.models.request.RawData
import support.UnitSpec

class RequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  implicit val correlationId: String = "X-123"
  case class Raw(nino: String) extends RawData
  case class Request(nino: Nino)

  trait Test {
    test =>

    val validator: Validator[Raw]

    val parser: RequestParser[Raw, Request] = new RequestParser[Raw, Request] {
      val validator: Validator[Raw] = test.validator

      protected def requestFor(data: Raw): Request = Request(Nino(data.nino))
    }
  }

  "parse" should {
    "return a Request" when {
      "the validator returns no errors" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => Nil

        parser.parseRequest(Raw(nino)) shouldBe Right(Request(Nino(nino)))
      }
    }

    "return a single error" when {
      "the validator returns a single error" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => List(NinoFormatError)

        parser.parseRequest(Raw(nino)) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
    }

    "return multiple errors" when {
      "the validator returns multiple errors" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => List(NinoFormatError, RuleIncorrectOrEmptyBodyError)

        parser.parseRequest(Raw(nino)) shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }

}

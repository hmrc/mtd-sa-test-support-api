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

package api.connectors.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Reads}
import support.UnitSpec
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import api.connectors.DownstreamOutcome
import api.models.errors._
import api.models.outcomes.ResponseWrapper

// WLOG if Reads tested elsewhere
case class SomeModel(data: String)

object SomeModel {
  implicit val reads: Reads[SomeModel] = Json.reads
}

class StandardDownstreamHttpParserSpec extends UnitSpec {

  val method = "POST"
  val url    = "test-url"

  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  import api.connectors.httpparsers.StandardDownstreamHttpParser._

  val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

  val data                  = "someData"
  val expectedJson: JsValue = Json.obj("data" -> data)

  val model: SomeModel                     = SomeModel(data)
  val response: ResponseWrapper[SomeModel] = ResponseWrapper(correlationId, model)

  "The generic HTTP parser" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[SomeModel]] = implicitly

      "return a Right IFS response containing the model object if the response json corresponds to a model object" in {
        val httpResponse = HttpResponse(OK, expectedJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(response)
      }

      "return an outbound error if a model object cannot be read from the response json" in {
        val badFieldTypeJson: JsValue = Json.obj("incomeSourceId" -> 1234, "incomeSourceName" -> 1234)
        val httpResponse              = HttpResponse(OK, badFieldTypeJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val expected                  = ResponseWrapper(correlationId, OutboundError(InternalError))

        httpReads.read(method, url, httpResponse) shouldBe Left(expected)
      }

      handleErrorsCorrectly(httpReads)
      handleInternalErrorsCorrectly(httpReads)
      handleUnexpectedResponse(httpReads)
      handleBvrsCorrectly(httpReads)
    }

    "a success code is specified" must {
      "use that status code for success" in {
        implicit val successCode: SuccessCode                  = SuccessCode(PARTIAL_CONTENT)
        val httpReads: HttpReads[DownstreamOutcome[SomeModel]] = implicitly

        val httpResponse = HttpResponse(PARTIAL_CONTENT, expectedJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(response)
      }
    }
  }

  "The generic HTTP parser for empty response" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "receiving a 204 response" should {
        "return a Right IfsResponse with the correct correlationId and no responseData" in {
          val httpResponse = HttpResponse(NO_CONTENT, "", headers = Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
        }
      }

      handleErrorsCorrectly(httpReads)
      handleInternalErrorsCorrectly(httpReads)
      handleUnexpectedResponse(httpReads)
      handleBvrsCorrectly(httpReads)
    }

    "a success code is specified" must {
      implicit val successCode: SuccessCode             = SuccessCode(PARTIAL_CONTENT)
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "use that status code for success" in {
        val httpResponse = HttpResponse(PARTIAL_CONTENT, "", headers = Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  val singleErrorJson: JsValue = Json.parse(
    """
      |{
      |   "code": "CODE",
      |   "reason": "MESSAGE"
      |}
    """.stripMargin
  )

  val multipleErrorsJson: JsValue = Json.parse(
    """
      |{
      |   "failures": [
      |       {
      |           "code": "CODE 1",
      |           "reason": "MESSAGE 1"
      |       },
      |       {
      |           "code": "CODE 2",
      |           "reason": "MESSAGE 2"
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val malformedErrorJson: JsValue = Json.parse(
    """
      |{
      |   "coed": "CODE",
      |   "resaon": "MESSAGE"
      |}
    """.stripMargin
  )

  private def handleErrorsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    Seq(BAD_REQUEST, NOT_FOUND, FORBIDDEN, CONFLICT, GONE).foreach(responseCode =>
      s"receiving a $responseCode response" should {
        "be able to parse a single error" in {
          val httpResponse = HttpResponse(responseCode, singleErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(
            ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("CODE"))))
        }

        "be able to parse multiple errors" in {
          val httpResponse = HttpResponse(responseCode, multipleErrorsJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe {
            Left(ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("CODE 1"), DownstreamErrorCode("CODE 2")))))
          }
        }

        "return an outbound error when the error returned doesn't match the Error model" in {
          val httpResponse = HttpResponse(responseCode, malformedErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
        }
      })

  private def handleInternalErrorsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach(responseCode =>
      s"receiving a $responseCode response" should {
        "return an outbound error when the error returned matches the Error model" in {
          val httpResponse = HttpResponse(responseCode, singleErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
        }

        "return an outbound error when the error returned doesn't match the Error model" in {
          val httpResponse = HttpResponse(responseCode, malformedErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
        }
      })

  private def handleUnexpectedResponse[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    "receiving an unexpected response" should {
      val responseCode = 499
      "return an outbound error when the error returned matches the Error model" in {
        val httpResponse = HttpResponse(responseCode, singleErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
      }

      "return an outbound error when the error returned doesn't match the Error model" in {
        val httpResponse = HttpResponse(responseCode, malformedErrorJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
      }
    }

  private def handleBvrsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit = {

    val singleBvrJson = Json.parse("""
        |{
        |   "bvrfailureResponseElement": {
        |     "validationRuleFailures": [
        |       {
        |         "id": "BVR1"
        |       },
        |       {
        |         "id": "BVR2"
        |       }
        |     ]
        |   }
        |}
      """.stripMargin)

    s"receiving a response with a bvr errors" should {
      "return an outbound BUSINESS_ERROR error containing the BVR ids" in {
        val httpResponse = HttpResponse(BAD_REQUEST, singleBvrJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe
          Left(
            ResponseWrapper(correlationId, OutboundError(BVRError, Some(Seq(MtdError("BVR1", "", BAD_REQUEST), MtdError("BVR2", "", BAD_REQUEST))))))
      }
    }
  }

}

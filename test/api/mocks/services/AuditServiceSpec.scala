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

package api.mocks.services

import api.models.audit.AuditEvent
import api.services.{AuditService, ServiceSpec}
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends ServiceSpec {

  private trait Test {
    val mockedAppName                      = "sample-application"
    val mockAuditConnector: AuditConnector = mock[AuditConnector]
    val mockConfig: Configuration          = mock[Configuration]

    (mockConfig
      .get(_: String)(_: play.api.ConfigLoader[String]))
      .expects(*, *)
      .returns(mockedAppName)

    lazy val target = new AuditService(mockAuditConnector, mockConfig)
  }

  "AuditService" when {
    "auditing an event" should {
      val auditType                     = "auditType"
      val transactionName               = "transaction-name"
      val eventDetails                  = "EventDetails"
      val expected: Future[AuditResult] = Future.successful(Success)
      "return a successful audit result" in new Test {

        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returns(expected)

        val event = AuditEvent(auditType, transactionName, eventDetails)
        target.auditEvent(event) shouldBe expected
      }

      "generates an event with the correct auditSource" in new Test {
        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(
            where { (eventArg: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
              eventArg.auditSource == mockedAppName // <- assertion in mock
            }
          )
          .returns(expected)

        val event = AuditEvent(auditType, transactionName, eventDetails)

        target.auditEvent(event)
      }

      "generates an event with the correct auditType" in new Test {
        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(
            where { (eventArg: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
              eventArg.auditType == auditType // <- assertion in mock
            }
          )
          .returns(expected)

        val event = AuditEvent(auditType, transactionName, eventDetails)

        target.auditEvent(event)
      }

      "generates an event with the correct details" in new Test {
        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(
            where { (eventArg: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
              eventArg.detail == Json.toJson(eventDetails) // <- assertion in mock
            }
          )
          .returns(expected)

        val event = AuditEvent(auditType, transactionName, eventDetails)

        target.auditEvent(event)
      }

      "generates an event with the correct transactionName" in new Test {
        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(
            where { (eventArg: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
              eventArg.tags.exists(tag => tag == "transactionName" -> transactionName) // <- assertion in mock
            }
          )
          .returns(expected)

        val event = AuditEvent(auditType, transactionName, eventDetails)

        target.auditEvent(event)
      }
    }
  }

}

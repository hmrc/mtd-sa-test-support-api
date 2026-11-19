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

package api.config

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.Configuration

trait MockAppConfig extends TestSuite with MockFactory {

  implicit val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    // Stub Config
    def stubBaseUrl: CallHandler[String]                         = (() => mockAppConfig.stubBaseUrl).expects()
    def stubEnv: CallHandler[String]                             = (() => mockAppConfig.stubEnv).expects()
    def stubToken: CallHandler[String]                           = (() => mockAppConfig.stubToken).expects()
    def stubEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.stubEnvironmentHeaders).expects()

    // API Config
    def featureSwitches: CallHandler[Configuration]             = (() => mockAppConfig.featureSwitches).expects()
    def apiGatewayContext: CallHandler[String]                  = (() => mockAppConfig.apiGatewayContext).expects()
    def apiStatus(status: String): CallHandler[String]          = (mockAppConfig.apiStatus: String => String).expects(status)
    def endpointsEnabled(version: String): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled: String => Boolean).expects(version)

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] = (() => mockAppConfig.confidenceLevelConfig).expects()
  }

}

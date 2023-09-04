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

package config

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

trait AppConfig {

  def stubDownstreamConfig: DownstreamConfig
  def businessDetailsConfig: DownstreamConfig

  // API Config
  def apiGatewayContext: String
  def confidenceLevelConfig: ConfidenceLevelConfig
  def apiStatus(version: String): String
  def featureSwitches: Configuration
  def endpointsEnabled(version: String): Boolean

}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {

  lazy val stubDownstreamConfig: DownstreamConfig = {
    val stubBaseUrl            = config.baseUrl("stub")
    val stubEnvironmentHeaders = configuration.getOptional[Seq[String]]("microservice.services.stub.environmentHeaders")

    DownstreamConfig(baseUrl = stubBaseUrl, environmentHeaders = stubEnvironmentHeaders)
  }

  lazy val businessDetailsConfig: DownstreamConfig = {
    // TODO: Get configuration for business details API
    // We need this to implement HATEOAS links (see here: https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=691961861 )
    // For now, this will just return the stub base URL + config as a placeholder
    val businessDetailsBaseUrl            = config.baseUrl("stub")
    val businessDetailsEnvironmentHeaders = configuration.getOptional[Seq[String]]("microservice.services.stub.environmentHeaders")

    DownstreamConfig(baseUrl = businessDetailsBaseUrl, environmentHeaders = businessDetailsEnvironmentHeaders)
  }

  // API Config
  val apiGatewayContext: String                    = config.getString("api.gateway.context")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")
  def apiStatus(version: String): String           = config.getString(s"api.$version.status")
  def featureSwitches: Configuration               = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)
  def endpointsEnabled(version: String): Boolean   = config.getBoolean(s"api.$version.endpoints.enabled")

}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}

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

import api.controllers.ControllerBaseSpec
import com.typesafe.config.ConfigFactory
import config.rewriters._
import controllers.{AssetsConfiguration, DefaultAssetsMetadata, RewriteableAssets}
import definition.ApiDefinitionFactory
import mocks.MockAppConfig
import org.scalatest.OptionValues
import play.api.Configuration
import play.api.http.{DefaultFileMimeTypes, DefaultHttpErrorHandler, FileMimeTypesConfiguration, HttpConfiguration}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentationControllerSpec extends ControllerBaseSpec with MockAppConfig with OptionValues {

  "/file endpoint" should {
    "return a file" in new Test {
      MockAppConfig.endpointsEnabled("1.0").anyNumberOfTimes() returns true
      val response: Future[Result] = requestAsset("application.yaml")
      status(response) shouldBe OK
      await(response).body.contentLength.value should be > 0L
    }
  }

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    protected def featureEnabled: Boolean = true

    protected def requestAsset(filename: String, accept: String = "text/yaml"): Future[Result] =
      controller.asset("1.0", filename)(fakeGetRequest.withHeaders(ACCEPT -> accept))

    MockAppConfig.featureSwitches returns Configuration("openApiFeatureTest.enabled" -> featureEnabled)

    private val apiFactory = new ApiDefinitionFactory(mockAppConfig)

    private val config    = new Configuration(ConfigFactory.load())
    private val mimeTypes = HttpConfiguration.parseFileMimeTypes(config) ++ Map("yaml" -> "text/yaml", "md" -> "text/markdown")

    private val assetsMetadata =
      new DefaultAssetsMetadata(
        AssetsConfiguration(textContentTypes = Set("text/yaml", "text/markdown")),
        path => {
          Option(getClass.getResource(path))
        },
        new DefaultFileMimeTypes(FileMimeTypesConfiguration(mimeTypes))
      )

    private val errorHandler = new DefaultHttpErrorHandler()

    private val docRewriters = new DocumentationRewriters(
      new OasFeatureRewriter()(mockAppConfig)
    )

    private val assets       = new RewriteableAssets(errorHandler, assetsMetadata, mockAppConfig)
    protected val controller = new DocumentationController(apiFactory, docRewriters, assets, cc)
  }

}

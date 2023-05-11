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

package routing

import com.typesafe.config.ConfigFactory
import definition.Versions
import mocks.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite with ScalaCheckPropertyChecks {

  val defaultRouter: Router     = mock[Router]
  val v1Routes: v1.Routes       = app.injector.instanceOf[v1.Routes]

  private def newVersionRoutingMap(v2r7cEnabled: Boolean) = {
    MockAppConfig.featureSwitches.returns(Configuration(ConfigFactory.parseString(s"v2r7c-endpoints.enabled = $v2r7cEnabled")))

    VersionRoutingMapImpl(
      appConfig = mockAppConfig,
      defaultRouter = defaultRouter,
      v1Router = v1Routes
    )
  }

  "map" when {
    "routing a v1 request" should {
      "route to v1.routes" in
        forAll { v2r7cEnabled: Boolean =>
          val versionRoutingMap = newVersionRoutingMap(v2r7cEnabled)
          versionRoutingMap.map(Versions.VERSION_1) shouldBe v1Routes
        }
    }
  }
}

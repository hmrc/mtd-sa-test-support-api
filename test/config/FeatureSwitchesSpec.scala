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

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchesSpec extends UnitSpec with ScalaCheckPropertyChecks {

  "isVersionEnabled()" should {
    val configuration = Configuration(
      "version-1.enabled" -> true,
      "version-2.enabled" -> false
    )
    val featureSwitches = FeatureSwitches(configuration)

    "return false" when {
      "the version is blank" in {
        featureSwitches.isVersionEnabled("") shouldBe false
      }

      "the version is an invalid format" in {
        featureSwitches.isVersionEnabled("ABCDE-1") shouldBe false
        featureSwitches.isVersionEnabled("1.") shouldBe false
        featureSwitches.isVersionEnabled("1.ABC") shouldBe false
      }

      "the version isn't in the config" in {
        featureSwitches.isVersionEnabled("3.0") shouldBe false
      }

      "the version is disabled in the config" in {
        featureSwitches.isVersionEnabled("2.0") shouldBe false
      }
    }

    "return true" when {
      "the version is enabled in the config" in {
        featureSwitches.isVersionEnabled("1.0") shouldBe true
      }
    }
  }

  "a feature switch" should {
    val switchName = "someSwitch"

    "be true" when {
      "absent from the config" in {
        val configuration   = Configuration.empty
        val featureSwitches = FeatureSwitches(configuration)

        featureSwitches.isEnabled(switchName) shouldBe true
      }

      "explicitly enabled" in {
        val configuration   = Configuration(s"$switchName.enabled" -> true)
        val featureSwitches = FeatureSwitches(configuration)

        featureSwitches.isEnabled(switchName) shouldBe true
      }
    }

    "be false" when {
      "explicitly disabled" in {
        val configuration   = Configuration(s"$switchName.enabled" -> false)
        val featureSwitches = FeatureSwitches(configuration)

        featureSwitches.isEnabled(switchName) shouldBe false
      }
    }
  }

}

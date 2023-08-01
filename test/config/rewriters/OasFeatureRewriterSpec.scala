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

package config.rewriters

import config.rewriters.DocumentationRewriters.CheckRewrite
import controllers.Rewriter
import mocks.MockAppConfig
import play.api.Configuration
import support.UnitSpec

class OasFeatureRewriterSpec extends UnitSpec with MockAppConfig {

  private def setupCheckAndRewrite(oasFeatureEnabled: Boolean, versionEnabled: Boolean): (CheckRewrite, Rewriter) = {
    MockAppConfig.featureSwitches returns Configuration(
      "oasFeature.enabled" -> oasFeatureEnabled
    )

    MockAppConfig.endpointsEnabled("1.0").anyNumberOfTimes() returns versionEnabled

    val rewriter = new OasFeatureRewriter()(mockAppConfig)
    rewriter.rewriteOasFeature.asTuple
  }

  "check and rewrite" should {

    "indicate whether it wants to rewrite the file" when {
      "1.0 endpoints are disabled" in {
        val (check, _) = setupCheckAndRewrite(oasFeatureEnabled = true, versionEnabled = false)

        val result = check("1.0", "any-file.yaml")
        result shouldBe false
      }

      "1.0 endpoints are enabled" in {
        val (check, _) = setupCheckAndRewrite(oasFeatureEnabled = true, versionEnabled = true)

        val result = check("1.0", "any-file.yaml")
        result shouldBe true
      }
    }

    "rewrite" when {

      val yaml =
        """
          |summary: Retrieve Employment Expenses
          |description: |
          |  This endpoint enables you to retrieve existing employment expenses.
          |  A National Insurance number and tax year must be provided.
          |
          |  ### Test data
          |  {{#if (enabled "oasFeature")}}
          |  <p>Scenario simulations using Gov-Test-Scenario headers ARE ONLY AVAILABLE IN the sandbox environment.</p>
          |  {{else}}
          |  <p>Scenario simulations using Gov-Test-Scenario headers are only available in the sandbox environment.</p>
          |  {{/if}}
          |
          |tags:
          |  - Employment Expenses
          |""".stripMargin

      "the feature isn't enabled" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = false, versionEnabled = true)

        val expected =
          s"""
             |summary: Retrieve Employment Expenses
             |description: |
             |  This endpoint enables you to retrieve existing employment expenses.
             |  A National Insurance number and tax year must be provided.
             |
             |  ### Test data
             |${" "}${" "}
             |  <p>Scenario simulations using Gov-Test-Scenario headers are only available in the sandbox environment.</p>
             |${" "}${" "}
             |
             |tags:
             |  - Employment Expenses
             |""".stripMargin

        val result = rewrite("/...", "something.yaml", yaml)
        result shouldBe expected
      }

      "the feature is enabled" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = true, versionEnabled = true)

        val expected =
          s"""
          |summary: Retrieve Employment Expenses
          |description: |
          |  This endpoint enables you to retrieve existing employment expenses.
          |  A National Insurance number and tax year must be provided.
          |
          |  ### Test data
          |${" "}${" "}
          |  <p>Scenario simulations using Gov-Test-Scenario headers ARE ONLY AVAILABLE IN the sandbox environment.</p>
          |${" "}${" "}
          |
          |tags:
          |  - Employment Expenses
          |""".stripMargin

        val result = rewrite("/...", "something.yaml", yaml)
        result shouldBe expected
      }
    }
  }

}

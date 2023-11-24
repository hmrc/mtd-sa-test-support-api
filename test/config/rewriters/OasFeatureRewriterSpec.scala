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

  private def setupCheckAndRewrite(oasFeatureEnabled: Boolean, oasFeatureReleasedInProd: Boolean): (CheckRewrite, Rewriter) = {
    MockAppConfig.featureSwitches returns Configuration(
      "oasFeature.enabled"                -> oasFeatureEnabled,
      "oasFeature.released-in-production" -> oasFeatureReleasedInProd
    )

    val rewriter = new OasFeatureRewriter()
    rewriter.rewriteOasFeature.asTuple
  }

  "check" should {
    "always return true" in {
      List(
        (true, true),
        (true, false),
        (false, true),
        (false, false)
      ).foreach { case (enabled, enabledInProd) =>
        val (check, _) = setupCheckAndRewrite(oasFeatureEnabled = enabled, oasFeatureReleasedInProd = enabledInProd)

        val result = check("1.0", "any-file.yaml")
        result shouldBe true
      }
    }
  }

  "rewrite" should {

    val yaml =
      """
        |summary: Retrieve Employment Expenses
        |description: |
        |  This endpoint enables you to retrieve existing employment expenses.
        |  A National Insurance number and tax year must be provided.
        |
        |  ### Test data
        |  {{#if (enabled "oasFeature")}}
        |  <p>{{#unless (releasedInProduction "oasFeature")}}[Test Only] {{/unless}}Scenario simulations using Gov-Test-Scenario headers are only available in the sandbox environment.</p>
        |  {{/if}}
        |
        |tags:
        |  - Employment Expenses
        |""".stripMargin

    "show the field without [Test Only]" when {
      "the feature is enabled in environment and in prod" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = true, oasFeatureReleasedInProd = true)

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
    }

    "show the field with [Test Only]" when {
      "the feature is enabled in environment but not in prod" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = true, oasFeatureReleasedInProd = false)

        val expected =
          s"""
             |summary: Retrieve Employment Expenses
             |description: |
             |  This endpoint enables you to retrieve existing employment expenses.
             |  A National Insurance number and tax year must be provided.
             |
             |  ### Test data
             |${" "}${" "}
             |  <p>[Test Only] Scenario simulations using Gov-Test-Scenario headers are only available in the sandbox environment.</p>
             |${" "}${" "}
             |
             |tags:
             |  - Employment Expenses
             |""".stripMargin

        val result = rewrite("/...", "something.yaml", yaml)
        result shouldBe expected
      }
    }

    "not show the field" when {
      "the feature is disabled in environment but enabled in prod" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = false, oasFeatureReleasedInProd = true)

        val expected =
          s"""
             |summary: Retrieve Employment Expenses
             |description: |
             |  This endpoint enables you to retrieve existing employment expenses.
             |  A National Insurance number and tax year must be provided.
             |
             |  ### Test data
             |${" "}${" "}
             |
             |tags:
             |  - Employment Expenses
             |""".stripMargin

        val result = rewrite("/...", "something.yaml", yaml)
        result shouldBe expected
      }

      "the feature isn't enabled in environment and prod" in {
        val (_, rewrite) = setupCheckAndRewrite(oasFeatureEnabled = false, oasFeatureReleasedInProd = false)

        val expected =
          s"""
             |summary: Retrieve Employment Expenses
             |description: |
             |  This endpoint enables you to retrieve existing employment expenses.
             |  A National Insurance number and tax year must be provided.
             |
             |  ### Test data
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

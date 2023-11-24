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

import com.github.jknack.handlebars.Options
import config.rewriters.DocumentationRewriters.CheckAndRewrite
import config.{AppConfig, FeatureSwitches}

import javax.inject.{Inject, Singleton}

/** Checks whether the feature is enabled in the current environment e.g. ET/Sandbox.
  */
@Singleton class OasFeatureRewriter @Inject() (implicit val appConfig: AppConfig) extends HandlebarsRewriter {

  private val fs = FeatureSwitches(appConfig.featureSwitches)

  /*
    enabled - is this feature enabled in the current env (ET/sandbox)
    - use this to determine whether to show a feature
    - or with HB "if" to determine whether to include the feature in an example JSON response
   */
  hb.registerHelper(
    "enabled",
    (featureName: String, _: Options) => {
      if (fs.isEnabled(featureName)) "true" else null // javascript "truthy"
    })

  /*
  releasedInProduction - is this feature enabled here AND in prod
    - use this with HB "unless" to determine whether to show "Test only"
   */
  hb.registerHelper(
    "releasedInProduction",
    (featureName: String, _: Options) => {
      if (fs.isEnabled(featureName) && fs.isReleasedInProduction(featureName)) "true" else null // javascript "truthy"
    }
  )

  val rewriteOasFeature: CheckAndRewrite = CheckAndRewrite(
    check = (_, _) => true,
    rewrite = (_, _, contents) => rewrite(contents, fs)
  )

}

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

import config.rewriters.DocumentationRewriters.CheckAndRewrite
import controllers.Rewriter

import javax.inject.{Inject, Singleton}

@Singleton class DocumentationRewriters @Inject() (oasFeatureRewriter: OasFeatureRewriter) {

  val rewriteables: Seq[CheckAndRewrite] =
    List(
      oasFeatureRewriter.rewriteOasFeature
    )

}

object DocumentationRewriters {

  trait CheckRewrite {
    def apply(version: String, filename: String): Boolean
  }

  case class CheckAndRewrite(check: CheckRewrite, rewrite: Rewriter) {

    def maybeRewriter(version: String, filename: String): Option[Rewriter] =
      if (check(version, filename)) Some(rewrite) else None

    val asTuple: (CheckRewrite, Rewriter) = (check, rewrite)
  }

}

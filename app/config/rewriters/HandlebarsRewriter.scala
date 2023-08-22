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

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import config.AppConfig

trait HandlebarsRewriter {
  implicit val appConfig: AppConfig

  protected val hb: Handlebars =
    new Handlebars()
      .`with`(new ConcurrentMapTemplateCache()) // so each file is only compiled once

  protected def rewrite(contents: String, context: AnyRef): String = {
    if (contents.contains(("{{#"))) { // avoids every OAS file being cached as a Template
      val template = hb.compileInline(contents)
      template.apply(context)
    } else {
      contents
    }
  }

}

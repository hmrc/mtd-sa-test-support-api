/*
 * Copyright 2022 HM Revenue & Customs
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

import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "8.1.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"                %% "cats-core"                 % "2.9.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.neovisionaries"            % "nv-i18n"                   % "1.29",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.15.2",
    "com.github.jknack"             % "handlebars"                % "4.3.1"
  )

  def test(scope: String = "test, it"): Seq[sbt.ModuleID] = Seq(
    "org.scalatest"       %% "scalatest"              % "3.2.15"             % scope,
    "com.vladsch.flexmark" % "flexmark-all"           % "0.64.6"             % scope,
    "org.scalatestplus"   %% "scalacheck-1-15"        % "3.2.11.0"           % scope,
    "org.scalamock"       %% "scalamock"              % "5.2.0"              % scope,
    "org.playframework"   %% "play-test"              % PlayVersion.current  % scope,
    "uk.gov.hmrc"         %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
    "org.wiremock"         % "wiremock"               % "3.0.4"              % scope,
    "io.swagger.parser.v3" % "swagger-parser-v3"      % "2.1.12"             % scope
  )

}

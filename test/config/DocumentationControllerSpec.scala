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
import config.rewriters.DocumentationRewriters.{CheckAndRewrite, CheckRewrite}
import config.rewriters.*
import controllers.{RewriteableAssets, Rewriter}
import definition.ApiDefinitionFactory
import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import play.api.mvc.Results.Ok
import play.api.mvc.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentationControllerSpec extends ControllerBaseSpec with MockAppConfig {

  "asset rewriting" when {
    "all rewriters are to be used" must {
      "use all rewriters" in new Test {
        willUse(check1) returns true
        willUse(check2) returns true
        willRewriteUsing(Seq(rewriter1, rewriter2)) returns rewrittenOkAction

        val response: Future[Result] = requestAsset(filename)
        status(response) shouldBe OK
        contentAsString(response) shouldBe rewrittenContent
      }
    }

    "some rewriters are not be used" must {
      "only use the required rewriters" in new Test {
        willUse(check1) returns false
        willUse(check2) returns true
        willRewriteUsing(Seq(rewriter2)) returns rewrittenOkAction

        val response: Future[Result] = requestAsset(filename)
        status(response) shouldBe OK
        contentAsString(response) shouldBe rewrittenContent
      }
    }
  }

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val version          = "1.0"
    val filename         = "someFile.ext"
    val rewrittenContent = "RE-WRITTEN"

    protected def requestAsset(filename: String, accept: String = "text/yaml"): Future[Result] =
      controller.asset("1.0", filename)(fakeGetRequest.withHeaders(ACCEPT -> accept))

    private val apiFactory = new ApiDefinitionFactory(mockAppConfig)
    private val actionBuilder = DefaultActionBuilder(BodyParsers.utils.ignore[AnyContent](AnyContentAsEmpty))

    protected val check1: CheckRewrite = mock[CheckRewrite]
    protected val rewriter1: Rewriter  = mock[Rewriter]

    protected val check2: CheckRewrite = mock[CheckRewrite]
    protected val rewriter2: Rewriter  = mock[Rewriter]

    private val docRewriters = new DocumentationRewriters {

      override def rewriteables: Seq[CheckAndRewrite] = {
        Seq(CheckAndRewrite(check1, rewriter1), CheckAndRewrite(check2, rewriter2))
      }

    }

    protected def willUse(check: CheckRewrite): CallHandler[Boolean] =
      (check(_: String, _: String)).expects(version, filename)

    protected def willRewriteUsing(rewriters: Seq[Rewriter]): CallHandler[Action[AnyContent]] =
      (rewriteableAssets.rewriteableAt(_: String, _: String, _: Seq[Rewriter])).expects(s"/public/api/conf/$version", filename, rewriters)

    protected def rewrittenOkAction: Action[AnyContent] = actionBuilder { (_: Request[AnyContent]) => Ok(rewrittenContent) }

    protected val rewriteableAssets: RewriteableAssets = mock[RewriteableAssets]
    protected val controller                           = new DocumentationController(apiFactory, docRewriters, rewriteableAssets, cc)
  }

}

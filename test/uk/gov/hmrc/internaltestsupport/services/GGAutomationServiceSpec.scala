/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.internaltestsupport.services

import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.core.read.ListAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import com.microsoft.playwright.*
import org.slf4j.LoggerFactory
import play.api.inject.DefaultApplicationLifecycle
import uk.gov.hmrc.internaltestsupport.models.*
import uk.gov.hmrc.internaltestsupport.models.ggauth.GGAuthResponse

import java.util.function.{Consumer, Predicate}
import java.util.regex.Pattern
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

class GGAutomationServiceSpec extends ServiceSpec {

  "GGAutomationService" should {
    "not initialize Playwright when constructed or stopped without a submission" in new Test {
      await(lifecycle.stop())
    }

    "complete the sign-in flow and extract the authorization code" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("location" -> "http://localhost:9000?code=auth-code&state=12345"))
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Right(ResponseWrapper(correlationId, GGAuthResponse("auth-code")))
      await(lifecycle.stop())
    }

    "accept an upper-case Location header" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("Location" -> "http://localhost:9000?code=auth-code"))
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Right(ResponseWrapper(correlationId, GGAuthResponse("auth-code")))
      await(lifecycle.stop())
    }

    "log browser events without interrupting the authorization flow" in new Test {
      val logger        = LoggerFactory.getLogger(service.getClass).asInstanceOf[Logger]
      val previousLevel = logger.getLevel
      val appender      = new ListAppender[ILoggingEvent]
      appender.start()
      logger.addAppender(appender)
      logger.setLevel(Level.DEBUG)
      try {
        expectBrowser()
        expectPage(emitEvents = true)
        expectForm()
        expectRedirect(Map("location" -> "http://localhost:9000?code=auth-code"))
        expectShutdown()

        await(service.submitForm(submission)) shouldBe Right(ResponseWrapper(correlationId, GGAuthResponse("auth-code")))
        await(lifecycle.stop())

        val messages = appender.list.asScala.map(_.getFormattedMessage)
        messages should contain allOf (
          "[context.onPage] new page created: https://example.test/page",
          "[page.onPopup] popup opened: https://example.test/page",
          "[request failed] https://example.test/request -> connection refused",
          "[context.onRequest] GET https://example.test/request",
          "[context.onResponse] 200 https://example.test/response"
        )
      } finally {
        logger.setLevel(previousLevel)
        logger.detachAppender(appender)
        appender.stop()
      }
    }

    "reuse the browser while closing each submission's page and context" in new Test {
      expectBrowser()
      (1 to 2).foreach { _ =>
        expectPage()
        expectForm()
        expectRedirect(Map("location" -> "http://localhost:9000?code=auth-code"))
        (() => page.close()).expects()
        (() => context.close()).expects()

        await(service.submitForm(submission)) shouldBe Right(ResponseWrapper(correlationId, GGAuthResponse("auth-code")))
      }
      (() => browser.close()).expects()
      (() => playwright.close()).expects()
      await(lifecycle.stop())
    }

    "return an error when the grant-scope response has no redirect header" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map.empty)
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Left(ErrorWrapper(correlationId, GrantScopeRetrievalError))
      await(lifecycle.stop())
    }

    "return a grant-scope error when the redirect Location is blank" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("location" -> " "))
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Left(ErrorWrapper(correlationId, GrantScopeRetrievalError))
      await(lifecycle.stop())
    }

    "return an error when the callback contains no OAuth code" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("location" -> "http://localhost:9000?state=12345"))
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Left(ErrorWrapper(correlationId, OAuthCodeRetrievalError))
      await(lifecycle.stop())
    }

    Seq(
      new TimeoutError("timed out") -> PWTimeoutError.withMessage("Timed out waiting for expected page navigation or response: timed out"),
      new PlaywrightException("browser failed")      -> PWError.withMessage("Playwright errored with response: browser failed"),
      new IllegalStateException("navigation failed") -> PWError.withMessage("Page navigation failed with response: navigation failed")
    ).foreach { case (exception, error) =>
      s"map ${exception.getClass.getSimpleName} and close the page and context" in new Test {
        expectBrowser()
        expectPage()
        (page.navigate(_: String, _: Page.NavigateOptions)).expects(*, *).throwing(exception)
        expectShutdown()

        await(service.submitForm(submission)) shouldBe Left(ErrorWrapper(correlationId, error))
        await(lifecycle.stop())
      }
    }

    "close Playwright if browser launch fails" in new Test {
      (() => playwright.chromium()).expects().returning(browserType)
      (browserType.launch(_: BrowserType.LaunchOptions)).expects(*).throwing(new PlaywrightException("launch failed"))
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWError.withMessage("Playwright errored with response: launch failed")))
      await(lifecycle.stop())
    }

    "preserve a launch timeout when Playwright cleanup also fails" in new Test {
      (() => playwright.chromium()).expects().returning(browserType)
      (browserType.launch(_: BrowserType.LaunchOptions)).expects(*).throwing(new TimeoutError("launch timed out"))
      (() => playwright.close()).expects().throwing(new PlaywrightException("close failed"))

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWTimeoutError.withMessage("Timed out waiting for expected page navigation or response: launch timed out")))
      await(lifecycle.stop())
    }

    "map a context creation failure to a browser error" in new Test {
      expectBrowser()
      (browser.newContext(_: Browser.NewContextOptions)).expects(*).throwing(new PlaywrightException("context failed"))
      (() => browser.close()).expects()
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWError.withMessage("Playwright errored with response: context failed")))
      await(lifecycle.stop())
    }

    "close the context when page creation fails" in new Test {
      expectBrowser()
      (browser.newContext(_: Browser.NewContextOptions)).expects(*).returning(context)
      (() => context.newPage()).expects().throwing(new PlaywrightException("page failed"))
      (() => context.close()).expects()
      (() => browser.close()).expects()
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWError.withMessage("Playwright errored with response: page failed")))
      await(lifecycle.stop())
    }

    "map a grant-scope timeout as a technical error rather than a missing redirect" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      (page
        .waitForResponse(_: Predicate[Response], _: Page.WaitForResponseOptions, _: Runnable))
        .expects(*, *, *)
        .throwing(new TimeoutError("grant-scope timed out"))
      expectShutdown()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWTimeoutError.withMessage("Timed out waiting for expected page navigation or response: grant-scope timed out")))
      await(lifecycle.stop())
    }

    "preserve a navigation timeout when cleanup also fails" in new Test {
      expectBrowser()
      expectPage()
      (page.navigate(_: String, _: Page.NavigateOptions)).expects(*, *).throwing(new TimeoutError("navigation timed out"))
      (() => page.close()).expects().throwing(new PlaywrightException("page close failed"))
      (() => context.close()).expects().throwing(new PlaywrightException("context close failed"))
      (() => browser.close()).expects()
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWTimeoutError.withMessage("Timed out waiting for expected page navigation or response: navigation timed out")))
      await(lifecycle.stop())
    }

    "map a context cleanup failure to a browser error" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("location" -> "http://localhost:9000?code=auth-code"))
      (() => page.close()).expects()
      (() => context.close()).expects().throwing(new PlaywrightException("context close failed"))
      (() => browser.close()).expects()
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWError.withMessage("Playwright errored with response: context close failed")))
      await(lifecycle.stop())
    }

    "close the context and map the error when closing the page fails" in new Test {
      expectBrowser()
      expectPage()
      expectForm()
      expectRedirect(Map("location" -> "http://localhost:9000?code=auth-code"))
      (() => page.close()).expects().throwing(new PlaywrightException("close failed"))
      (() => context.close()).expects()
      (() => browser.close()).expects()
      (() => playwright.close()).expects()

      await(service.submitForm(submission)) shouldBe Left(
        ErrorWrapper(correlationId, PWError.withMessage("Playwright errored with response: close failed")))
      await(lifecycle.stop())
    }
  }

  trait Test {
    val playwright  = mock[Playwright]
    val browserType = mock[BrowserType]
    val browser     = mock[Browser]
    val context     = mock[BrowserContext]
    val page        = mock[Page]
    val lifecycle   = new DefaultApplicationLifecycle
    val submission  = SubmitRequest("AA123456A", "XQIT12345678901")

    val service = new GGAutomationService(lifecycle, ExecutionContext.parasitic) {
      override protected def createPlaywright(): Playwright = playwright
    }

    def expectBrowser(): Unit = {
      (() => playwright.chromium()).expects().returning(browserType)
      (browserType
        .launch(_: BrowserType.LaunchOptions))
        .expects(where((options: BrowserType.LaunchOptions) => options.headless.booleanValue()))
        .returning(browser)
    }

    def expectPage(emitEvents: Boolean = false): Unit = {
      (browser.newContext(_: Browser.NewContextOptions)).expects(*).returning(context)
      (() => context.newPage()).expects().returning(page)
      (page.setDefaultTimeout(_: Double)).expects(15000d)
      if (emitEvents) {
        val request  = mock[Request]
        val response = mock[Response]
        (() => page.url()).expects().returning("https://example.test/page").twice()
        (() => request.url()).expects().returning("https://example.test/request").twice()
        (() => request.failure()).expects().returning("connection refused")
        (() => request.method()).expects().returning("GET")
        (() => response.status()).expects().returning(200)
        (() => response.url()).expects().returning("https://example.test/response")
        (context.onPage(_: Consumer[Page])).expects(*).onCall((listener: Consumer[Page]) => listener.accept(page))
        (page.onPopup(_: Consumer[Page])).expects(*).onCall((listener: Consumer[Page]) => listener.accept(page))
        (page.onRequestFailed(_: Consumer[Request])).expects(*).onCall((listener: Consumer[Request]) => listener.accept(request))
        (context.onRequest(_: Consumer[Request])).expects(*).onCall((listener: Consumer[Request]) => listener.accept(request))
        (context.onResponse(_: Consumer[Response])).expects(*).onCall((listener: Consumer[Response]) => listener.accept(response))
      } else {
        (context.onPage(_: Consumer[Page])).expects(*)
        (page.onPopup(_: Consumer[Page])).expects(*)
        (page.onRequestFailed(_: Consumer[Request])).expects(*)
        (context.onRequest(_: Consumer[Request])).expects(*)
        (context.onResponse(_: Consumer[Response])).expects(*)
      }
    }

    def expectForm(): Unit = {
      (page
        .navigate(_: String, _: Page.NavigateOptions))
        .expects("https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in", *)
        .returning(null)

      Seq(
        "#confidenceLevel"            -> "250",
        "#affinityGroupSelect"        -> "Individual",
        "#credentialStrength"         -> "strong",
        "[name='enrolment[0].state']" -> "Activated").foreach { case (selector, value) =>
        val locator = mock[Locator]
        (page.locator(_: String)).expects(selector).returning(locator)
        (locator.selectOption(_: String)).expects(value).returning(java.util.List.of(value))
      }
      Seq(
        "#nino" -> submission.nino,
        "#redirectionUrl" -> "https://www.qa.tax.service.gov.uk/oauth/authorize?client_id=PyWtcKfIuJ729591F3STlG7lSAXN&scope=read:self-assessment+write:self-assessment&response_type=code&redirect_uri=http://localhost:9000&state=12345",
        "[name='enrolment[0].name']"                   -> "HMRC-MTD-IT",
        "[name='enrolment[0].taxIdentifier[0].name']"  -> "MTDITID",
        "[name='enrolment[0].taxIdentifier[0].value']" -> submission.identifier
      ).foreach { case (selector, value) =>
        val locator = mock[Locator]
        (page.locator(_: String)).expects(selector).returning(locator)
        (locator.fill(_: String)).expects(value)
      }

      Seq(
        "/oauth/start"           -> "#submit",
        "/oauth/whatYouWillNeed" -> "a[data-module='govuk-button']:has-text('Continue')",
        "/oauth/grantscope"      -> "a[data-module='govuk-button']:has-text('Sign in to the HMRC online service')"
      ).foreach { case (path, selector) =>
        val response = mock[Response]
        val request  = mock[Request]
        val locator  = mock[Locator]
        (() => response.request()).expects().returning(request)
        (() => request.isNavigationRequest()).expects().returning(true)
        (() => response.url()).expects().returning(s"https://www.qa.tax.service.gov.uk$path").twice()
        (() => response.status()).expects().returning(200)
        (page.locator(_: String)).expects(selector).returning(locator)
        (() => locator.click()).expects()
        (page
          .waitForResponse(_: Predicate[Response], _: Page.WaitForResponseOptions, _: Runnable))
          .expects(*, *, *)
          .onCall { (predicate: Predicate[Response], options: Page.WaitForResponseOptions, trigger: Runnable) =>
            options.timeout shouldBe 15000d
            trigger.run()
            predicate.test(response) shouldBe true
            response
          }
        (page.waitForURL(_: Pattern)).expects(where((pattern: Pattern) => pattern.matcher(s"https://host$path?state=1").matches()))
      }
    }

    def expectRedirect(headers: Map[String, String]): Unit = {
      val response = mock[Response]
      val locator  = mock[Locator]
      (page.locator(_: String)).expects("#givePermission").returning(locator)
      (() => locator.click()).expects()
      (() => response.url()).expects().returning("https://www.qa.tax.service.gov.uk/oauth/grantscope")
      (() => response.status()).expects().returning(303)
      (() => response.headers())
        .expects()
        .returning(new java.util.HashMap[String, String]() {
          headers.foreach { case (key, value) => put(key, value) }
        })
      (page
        .waitForResponse(_: Predicate[Response], _: Page.WaitForResponseOptions, _: Runnable))
        .expects(*, *, *)
        .onCall { (predicate: Predicate[Response], options: Page.WaitForResponseOptions, trigger: Runnable) =>
          options.timeout shouldBe 5000d
          trigger.run()
          predicate.test(response) shouldBe true
          response
        }
    }

    def expectShutdown(): Unit = {
      (() => page.close()).expects()
      (() => context.close()).expects()
      (() => browser.close()).expects()
      (() => playwright.close()).expects()
    }

  }

}

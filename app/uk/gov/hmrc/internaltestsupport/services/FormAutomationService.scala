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

import com.microsoft.playwright.*
import com.microsoft.playwright.options.*
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.internaltestsupport.models.{FormSubmitRequest, FormSubmitResult}
import uk.gov.hmrc.internaltestsupport.utils.UrlParser

import java.util.regex.Pattern
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class FormAutomationService @Inject() (
                                        lifecycle: ApplicationLifecycle,
                                        @Named("playwrightEc") playwrightEc: ExecutionContext
                                      ) extends Logging {

  private val playwright: Playwright = Playwright.create()

  private val browser: Browser =
    playwright
      .chromium()
      .launch(
        new BrowserType.LaunchOptions()
          .setHeadless(false)
          .setSlowMo(50) // useful for debugging
      )

  lifecycle.addStopHook { () =>
    Future.successful {
      try browser.close()
      finally playwright.close()
    }
  }

  private object GGSelectors {
    val affinityGroupSelect      = "#affinityGroupSelect"
    val credentialStrengthSelect = "#credentialStrength"
    val confidenceLevelSelect    = "#confidenceLevel"

    val redirectUrlInput = "#redirectionUrl"
    val ninoInput        = "#nino"

    val enrolmentKeyInput    = "[name='enrolment[0].name']"
    val identifierNameInput  = "[name='enrolment[0].taxIdentifier[0].name']"
    val identifierValueInput = "[name='enrolment[0].taxIdentifier[0].value']"
    val enrolmentStateInput  = "[name='enrolment[0].state']"

    val submitButton = "#submit"
  }

  private object OauthSelectors {
    val startButton           = "[data-module='govuk-button']:has-text('Continue')"
    val whatYouWillNeedButton = "[data-module='govuk-button']:has-text('Sign in to the HMRC online service')"
    val givePermission        = "#givePermission"
  }

  private object MfaSelectors {
    val continueBtn        = "#continue"
    val factorMobile       = "#factor-mobile"
    val ukPhoneToggleClose = "#uk-phone-number-toggle-close"
    val phoneNumberInput   = "#phoneNumber"
    val accessCodeInput    = "#accessCode"
  }

  // TODO: get this from somewhere else
  private val clientId = "PyWtcKfIuJ729591F3STlG7lSAXN"

  def submitForm(req: FormSubmitRequest): Future[FormSubmitResult] =
    Future(blockingSubmit(req))(playwrightEc)

  private def blockingSubmit(req: FormSubmitRequest): FormSubmitResult = {
    val context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 720))
    val page    = context.newPage()

    context.onPage(p => logger.debug(s"[context.onPage] new page created: ${p.url()}"))
    page.onPopup(p => logger.debug(s"[page.onPopup] popup opened: ${p.url()}"))
    page.onRequestFailed(req => logger.debug(s"[request failed] ${req.url()} -> ${req.failure()}"))

    context.onRequest(r =>
      try logger.debug(s"[context.onRequest] ${r.url()}")
      catch { case _: Throwable => () })
    context.onResponse(r =>
      try logger.debug(s"[context.onResponse] ${r.url()} status=${r.status()}")
      catch { case _: Throwable => () })

    // We capture the grantscope 303 Location header below to obtain the
    // callback URL directly. Reading the Location header avoids navigating
    // the browser to the callback host (localhost) which may not be
    // responding in test environments.

    // Use request-provided timeout if available; otherwise default
    page.setDefaultTimeout(15000.toDouble)

    try {
      val ggUrl    = "https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in"
      val oauthUrl = "https://www.qa.tax.service.gov.uk/oauth"

      page.navigate(ggUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

      // --- GG sign-in stub form ---
      page.locator(GGSelectors.confidenceLevelSelect).selectOption("250")
      page.locator(GGSelectors.affinityGroupSelect).selectOption("Individual")
      page.locator(GGSelectors.credentialStrengthSelect).selectOption("strong")

      page.locator(GGSelectors.ninoInput).fill(req.nino)
      page
        .locator(GGSelectors.redirectUrlInput)
        .fill(
          s"$oauthUrl/authorize?client_id=$clientId&scope=read:self-assessment+write:self-assessment&response_type=code&redirect_uri=http://localhost:9000/callback&state=12345"
        )

      page.locator(GGSelectors.enrolmentKeyInput).fill("HMRC-MTD-IT")
      page.locator(GGSelectors.identifierNameInput).fill("MTDITID")
      page.locator(GGSelectors.identifierValueInput).fill(req.identifier)
      page.locator(GGSelectors.enrolmentStateInput).selectOption("Activated")

      page.locator(GGSelectors.submitButton).click()

      // Submit: wait for OAuth /start BEFORE clicking (avoids races)
      clickAndWaitPathSuffix(page, OauthSelectors.startButton, s"/oauth/start")

      // --- OAuth flow click-through ---
      clickAndWaitPathSuffix(page, OauthSelectors.whatYouWillNeedButton, s"/oauth/whatYouWillNeed")

      // Finally: grantscope and give permission
      confirmPathSuffix(page, s"/oauth/grantscope")

      // First, try to capture the redirect URL from the grantscope response's
      // Location header. The grantscope response is a 303 redirect whose
      // Location will point at the callback URL (with the code). Capturing
      // the Location header avoids navigating the browser to localhost and
      // prevents request failures against the callback server.
      var grantscopeLocation: Option[String] = None
      try {
        val resp = page.waitForResponse(
          (r: Response) => r.url().contains("/oauth/grantscope") && r.status() == 303,
          new Page.WaitForResponseOptions().setTimeout(5000),
          new Runnable {
            override def run(): Unit = page.locator(OauthSelectors.givePermission).click()
          }
        )
        if (resp != null) {
          val headers = resp.headers()
          val loc     = if (headers != null) Option(headers.get("location")) else None
          grantscopeLocation = loc
        }
      } catch {
        case _: Throwable => // ignore, fallbacks below will try other approaches
      }

      if (grantscopeLocation.isDefined) {
        // We have the callback URL from the Location header; use that.
        val finalCallbackUrl = grantscopeLocation.get

        // Extract code from URL using the shared parser
        val oauthCode = UrlParser.extractCode(finalCallbackUrl)

        return FormSubmitResult(oauthCode = Some(oauthCode), error = None)
      }

      // No grantscope Location header observed. At this point we no longer
      // attempt popup/page fallbacks by default to keep the flow simple and
      // deterministic. If you need older fallback behaviour, enable the
      // debug flag and reintroduce the fallback logic.
      throw new RuntimeException("grantscope Location header not observed; cannot retrieve oauth code")
    } catch {
      case NonFatal(e) =>
        FormSubmitResult(
          oauthCode = None,
          error = Some(e.getMessage)
        )
    } finally {
      try page.close()
      finally context.close()
    }
  }

  private def confirmPathSuffix(page: Page, pathSuffix: String): Unit = {
    val pattern = Pattern.compile(".*" + Pattern.quote(pathSuffix) + "(\\?.*)?$")
    page.waitForURL(pattern)
  }

  private def clickAndWaitPathSuffix(page: Page, clickSelector: String, pathSuffix: String): Unit = {
    val pattern    = Pattern.compile(".*" + Pattern.quote(pathSuffix) + "(\\?.*)?$")
    val wait: Unit = page.waitForURL(pattern)
    page.locator(clickSelector).click()
    wait
  }

}

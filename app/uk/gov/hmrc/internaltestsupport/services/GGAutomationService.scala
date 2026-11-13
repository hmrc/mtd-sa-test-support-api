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

import api.controllers.RequestContext
import api.models.errors.{ErrorWrapper, MtdError}
import api.models.outcomes.ResponseWrapper
import api.services.{BaseService, ServiceOutcome}
import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitUntilState
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.internaltestsupport.models.*
import uk.gov.hmrc.internaltestsupport.models.ggauth.GGAuthResponse
import uk.gov.hmrc.internaltestsupport.utils.UrlParser

import java.util.regex.Pattern
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using
import scala.util.control.NonFatal

@Singleton
class GGAutomationService @Inject() (
    lifecycle: ApplicationLifecycle,
    @Named("playwrightEc") playwrightEc: ExecutionContext
) extends BaseService {

  private var browserResources: Option[(Playwright, Browser)] = None

  protected def createPlaywright(): Playwright = Playwright.create()

  private def browser: Browser = synchronized {
    browserResources match {
      case Some((_, instance)) => instance
      case None =>
        val playwright = createPlaywright()
        try {
          val instance = playwright
            .chromium()
            .launch(new BrowserType.LaunchOptions().setHeadless(true))
          browserResources = Some(playwright -> instance)
          instance
        } catch {
          case NonFatal(e) =>
            try playwright.close()
            catch {
              case NonFatal(closeError) =>
                logger.warn(s"[GGAutomationService] Playwright cleanup raised ${closeError.getClass.getSimpleName} after browser launch failed")
            }
            throw e
        }
    }
  }

  lifecycle.addStopHook { () =>
    Future.successful {
      synchronized {
        browserResources.foreach { case (playwright, browser) =>
          try browser.close()
          finally playwright.close()
        }
        browserResources = None
      }
    }
  }

  // TODO: set this somewhere else.  Toggle this locally if we want noisy output; keep false for CI/normal runs.
  private val debugLogging: Boolean = true

  // TODO: get this from config / secure source
  private val clientId = "PyWtcKfIuJ729591F3STlG7lSAXN"

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
    val startButton           = "a[data-module='govuk-button']:has-text('Continue')"
    val whatYouWillNeedButton = "a[data-module='govuk-button']:has-text('Sign in to the HMRC online service')"
    val givePermission        = "#givePermission"
  }

  def submitForm(req: SubmitRequest)(implicit rc: RequestContext): Future[ServiceOutcome[GGAuthResponse]] =
    Future(blockingSubmit(req))(playwrightEc).recover {
      case to: TimeoutError =>
        logger.error(s"[GGAutomationService] Browser automation timed out [correlationId=${rc.correlationId}]")
        Left(
          ErrorWrapper(rc.correlationId, PWTimeoutError.withMessage(s"Timed out waiting for expected page navigation or response: ${to.getMessage}")))
      case pw: PlaywrightException =>
        logger.error(s"[GGAutomationService] Browser automation failed [correlationId=${rc.correlationId}]")
        Left(ErrorWrapper(rc.correlationId, PWError.withMessage(s"Playwright errored with response: ${pw.getMessage}")))
      case NonFatal(e) =>
        logger.error(s"[GGAutomationService] Browser automation raised ${e.getClass.getSimpleName} [correlationId=${rc.correlationId}]")
        Left(ErrorWrapper(rc.correlationId, PWError.withMessage(s"Page navigation failed with response: ${e.getMessage}")))
    }(playwrightEc)

  private def blockingSubmit(req: SubmitRequest)(implicit rc: RequestContext): ServiceOutcome[GGAuthResponse] = {
    Using.resource(browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 720))) { context =>
      Using.resource(context.newPage()) { page =>
        // Default timeout for all Playwright operations
        page.setDefaultTimeout(15000d)

        if (debugLogging) attachDebugLogging(context, page)

        val ggUrl    = "https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in"
        val oauthUrl = "https://www.qa.tax.service.gov.uk/oauth"

        page.navigate(ggUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

        // --- GG sign-in stub ---
        page.locator(GGSelectors.confidenceLevelSelect).selectOption("250")
        page.locator(GGSelectors.affinityGroupSelect).selectOption("Individual")
        page.locator(GGSelectors.credentialStrengthSelect).selectOption("strong")

        page.locator(GGSelectors.ninoInput).fill(req.nino)

        // Redirect into oauth-frontend authorize flow. (String representation handling left as-is per your note.)
        page
          .locator(GGSelectors.redirectUrlInput)
          .fill(
            s"$oauthUrl/authorize" +
              s"?client_id=$clientId" +
              s"&scope=read:self-assessment+write:self-assessment" +
              s"&response_type=code" +
              s"&redirect_uri=http://localhost:9000" +
              s"&state=12345"
          )

        page.locator(GGSelectors.enrolmentKeyInput).fill("HMRC-MTD-IT")
        page.locator(GGSelectors.identifierNameInput).fill("MTDITID")
        page.locator(GGSelectors.identifierValueInput).fill(req.identifier)
        page.locator(GGSelectors.enrolmentStateInput).selectOption("Activated")

        // GG submit
        waitForSuccessfulNavigationTo(page, "/oauth/start") {
          page.locator(GGSelectors.submitButton).click()
        }
        confirmPathSuffix(page, "/oauth/start")

        // /oauth/start
        waitForSuccessfulNavigationTo(page, "/oauth/whatYouWillNeed") {
          page.locator(OauthSelectors.startButton).click()
        }
        confirmPathSuffix(page, "/oauth/whatYouWillNeed")

        // /oauth/whatYouWillNeed
        waitForSuccessfulNavigationTo(page, "/oauth/grantscope") {
          page.locator(OauthSelectors.whatYouWillNeedButton).click()
        }
        confirmPathSuffix(page, "/oauth/grantscope")

        // On grantscope: click givePermission and capture the 303 redirect Location header,
        // then extract the code.
        codeFromGivePermission(page) match {
          case Right(resp) => Right(ResponseWrapper(rc.correlationId, resp))
          case Left(err) =>
            logger.warn(s"[GGAutomationService] Authorization flow failed: ${err.code} [correlationId=${rc.correlationId}]")
            Left(ErrorWrapper(rc.correlationId, err))
        }

      }
    }
  }

  private def codeFromGivePermission(page: Page): Either[MtdError, GGAuthResponse] =
    for {
      callbackUrl <- grantscopeRedirectLocation(page)
      code        <- UrlParser.extractCode(callbackUrl)
    } yield GGAuthResponse(oauthCode = code)

  private def grantscopeRedirectLocation(page: Page): Either[MtdError, String] = {
    val response =
      page.waitForResponse(
        (r: Response) => r.url().contains("/oauth/grantscope") && r.status() == 303,
        new Page.WaitForResponseOptions().setTimeout(5000),
        new Runnable {
          override def run(): Unit =
            page.locator(OauthSelectors.givePermission).click()
        }
      )

    val localGrantscopeOpt =
      Option(response)
        .flatMap(r => Option(r.headers()))
        .flatMap(h => Option(h.get("location")).orElse(Option(h.get("Location"))))
        .filter(_.trim.nonEmpty)

    localGrantscopeOpt.toRight(GrantScopeRetrievalError)
  }

  // ---------- Waiting / navigation helpers (no waitForURL Runnable overload available) ----------

  /** Scoped wait: waits for the main document navigation response (status 200) whose URL contains `pathSuffix`, and runs `trigger` inside the wait
    * scope.
    */
  private def waitForSuccessfulNavigationTo(page: Page, pathSuffix: String, timeoutMs: Double = 15000)(trigger: => Unit): Unit = {
    page.waitForResponse(
      (r: Response) => {
        val req   = r.request()
        val isNav = req != null && req.isNavigationRequest
        val urlOk = r.url() != null && r.url().contains(pathSuffix)
        isNav && urlOk && r.status() / 100 == 2
      },
      new Page.WaitForResponseOptions().setTimeout(timeoutMs),
      new Runnable {
        override def run(): Unit = trigger
      }
    )
  }

  private def confirmPathSuffix(page: Page, pathSuffix: String): Unit = {
    val pattern = Pattern.compile(".*" + Pattern.quote(pathSuffix) + "(\\?.*)?$")
    page.waitForURL(pattern)
  }

  // ---------- Debug logging ----------

  private def attachDebugLogging(context: BrowserContext, page: Page): Unit = {
    context.onPage(p => logger.debug(s"[context.onPage] new page created: ${p.url()}"))
    page.onPopup(p => logger.debug(s"[page.onPopup] popup opened: ${p.url()}"))
    page.onRequestFailed(r => logger.debug(s"[request failed] ${r.url()} -> ${r.failure()}"))

    context.onRequest(r => logger.debug(s"[context.onRequest] ${r.method()} ${r.url()}"))
    context.onResponse(r => logger.debug(s"[context.onResponse] ${r.status()} ${r.url()}"))
  }

}

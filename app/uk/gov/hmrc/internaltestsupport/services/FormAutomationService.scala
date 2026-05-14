package uk.gov.hmrc.internaltestsupport.services

import com.microsoft.playwright.*
import com.microsoft.playwright.options.*

import javax.inject.*
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.internaltestsupport.models.{FormSubmitRequest, FormSubmitResult}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import java.util.regex.Pattern

@Singleton
class FormAutomationService @Inject()(
                                       lifecycle: ApplicationLifecycle,
                                       @Named("playwrightEc") playwrightEc: ExecutionContext
                                     ) {

  private val playwright: Playwright = Playwright.create()
  private val browser: Browser =
    playwright.chromium().launch(
      new BrowserType.LaunchOptions()
        .setHeadless(true)
      // .setSlowMo(50) // useful for debugging
    )

  lifecycle.addStopHook { () =>
    Future.successful {
      try browser.close()
      finally playwright.close()
    }
  }
  
  private object GGSelectors {
    val affinityGroupSelect         = "#affinityGroupSelect"
    val confidenceLevelSelect       = "#confidenceLevel"
    val confidenceStrengthSelect    = "#confidenceStrength"

    val ninoInput                   = "#nino"
    
    val enrolmentKeyInput           = "[id='enrolment[0].name']"
    val identifierNameInput         = "[id='enrolment[0].taxIdentifier[0].name']"
    val identifierValueInput        = "[id='enrolment[0].taxIdentifier[0].value']"
    val enrolmentStateInput         = "[id='enrolment[0].state']"

    val submitButton                = "#submit"
  }

  private object OauthSelectors {
    val govUkButton                 = "[data-module=govuk-button]"
    val givePermission              = "#givePermission"
  }

  private object MfaSelectors {
    val continueBtn                 = "#continue"
    val factorMobile                = "#factor-mobile"
    val ukPhoneToggleClose          = "#uk-phone-number-toggle-close"
    val phoneNumberInput            = "#phoneNumber"
    val accessCodeInput             = "#accessCode"
  }

  def submitForm(req: FormSubmitRequest): Future[FormSubmitResult] =
    Future(blockingSubmit(req))(playwrightEc)

  private def blockingSubmit(req: FormSubmitRequest): FormSubmitResult = {
    val context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 720))
    val page = context.newPage()

    // Use request-provided timeout if available; otherwise default
    page.setDefaultTimeout(15000.toDouble)

    try {
      val ggUrl        = "https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in"
      val oauthUrl     = "https://www.qa.tax.service.gov.uk/oauth"
      val mfaBaseUrl   = "https://www.qa.tax.service.gov.uk/multi-factor-authentication"

      page.navigate(ggUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

      // --- GG sign-in stub form ---
      page.locator(GGSelectors.confidenceLevelSelect).selectOption("250")
      page.locator(GGSelectors.affinityGroupSelect).selectOption("Individual")
      page.locator(GGSelectors.confidenceStrengthSelect).selectOption("strong")

      page.locator(GGSelectors.ninoInput).fill(req.nino)

      page.locator(GGSelectors.enrolmentKeyInput).fill("HMRC-MTD-IT")
      page.locator(GGSelectors.identifierNameInput).fill("MTDITID")
      page.locator(GGSelectors.identifierValueInput).fill(req.identifier)
      page.locator(GGSelectors.enrolmentStateInput).fill("Activated")

      // Submit: wait for OAuth /start BEFORE clicking (avoids races)
      clickAndWaitExactUrl(page, GGSelectors.submitButton, s"$oauthUrl/start")

      // --- OAuth flow click-through ---
      clickAndWaitExactUrl(page, OauthSelectors.govUkButton, s"$oauthUrl/whatYouWillNeed")

      // This step may branch (MFA vs straight to grantscope), so just click and then decide
      page.locator(OauthSelectors.govUkButton).click()

      multiFactorAuthIfRequired(page, mfaBaseUrl)

      // Finally: grantscope and give permission
      confirmExactUrl(page, s"$oauthUrl/grantscope")
      page.locator(OauthSelectors.givePermission).click()

      // Extract code from URL
      val oauthCode = oauthCodeFromUrl(page)

      FormSubmitResult(
        oauthCode = Some(oauthCode),
        error = None
      )
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

  private def multiFactorAuthIfRequired(page: Page, mfaBaseUrl: String): Unit = {
    // detect MFA by URL containing the service base url
    if (safeUrl(page).contains(mfaBaseUrl)) {

      page.locator(MfaSelectors.continueBtn).click()

      confirmPathSuffix(page, "/registration/choose-factor")
      page.locator(MfaSelectors.factorMobile).click()
      page.locator(MfaSelectors.continueBtn).click()

      confirmPathSuffix(page, "/registration/otp/phone-number/mobile")
      page.locator(MfaSelectors.ukPhoneToggleClose).click()
      page.locator(MfaSelectors.phoneNumberInput).fill("7712345678")
      page.locator(MfaSelectors.continueBtn).click()

      confirmPathSuffix(page, "/registration/otp/mobile")
      val accessCodeUrl: String = safeUrl(page)

      val mfaCode: String =
        accessCodeUrl.split("journey/")(1).split("/registration")(0)

      // test-only endpoint for access code
      val testOnlyUrl = s"$mfaBaseUrl/test-only/journey/$mfaCode/mobile/code"
      page.navigate(testOnlyUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

      // NOTE: if test-only returns JSON, you may need to parse page.content() instead.
      // Keeping your approach, but safer to read all text:
      val accessCodeText = page.textContent("body")
      val accessCode = accessCodeText.trim

      // go back and submit access code
      page.navigate(accessCodeUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))
      confirmPathSuffix(page, "/registration/otp/mobile")
      page.locator(MfaSelectors.accessCodeInput).fill(accessCode)
      page.locator(MfaSelectors.continueBtn).click()

      confirmPathSuffix(page, "/registration/success")
      page.locator(MfaSelectors.continueBtn).click()
    }
  }

  private def oauthCodeFromUrl(page: Page): String = {
    val u = page.url()
    val after = u.split("code=")(1)
    after.split("&")(0)
  }

  private def safeUrl(page: Page): String =
    try page.url() catch { case _: Throwable => "" }

  private def confirmExactUrl(page: Page, fullUrl: String): Unit = {
    val pattern = Pattern.compile("^" + Pattern.quote(fullUrl) + "$")
    page.waitForURL(pattern)
  }

  private def confirmPathSuffix(page: Page, pathSuffix: String): Unit = {
    val pattern = Pattern.compile(".*" + Pattern.quote(pathSuffix) + "(\\?.*)?$")
    page.waitForURL(pattern)
  }

  private def clickAndWaitExactUrl(page: Page, clickSelector: String, targetUrl: String): Unit = {
    val pattern = Pattern.compile("^" + Pattern.quote(targetUrl) + "$")
    val wait = page.waitForURL(pattern)
    page.locator(clickSelector).click()
    wait
  }
}
package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.TestUtils
import ai.bitlabs.sdk.findElementByDataTestId
import ai.bitlabs.sdk.hasElementWithDataTestId
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test

class OffersTests {
    private lateinit var intent: Intent

    @Before
    fun setUp() {
        intent = TestUtils.getIntentFor("offers")
    }

    @Test
    fun offerDetailsOpening() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(3000)

            onWebView()
                .withElement(findElementByDataTestId("offer-tile"))
                .perform(webClick())

            try {
                onWebView()
                    .withElement(findElementByDataTestId("offer-privacy-accept-button"))
                    .perform(webClick())
            } catch (e: Exception) {
            }

            Thread.sleep(2000)

            onWebView()
                .check(webContent(hasElementWithDataTestId("offer-tasks-modal")))
        }
    }
}
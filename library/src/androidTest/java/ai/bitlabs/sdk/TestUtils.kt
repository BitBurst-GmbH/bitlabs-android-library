package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.model.bitlabs.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_HEADER_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.views.BitLabsOfferwallActivity
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.web.matcher.DomMatchers.hasElementWithXpath
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.Locator

/**
 * Utility object for creating common Intents and Bundles for tests.
 */
object TestUtils {

    fun getIntentFor(displayMode: String, color: IntArray? = null): Intent {
        val url = WebActivityParams(
            BuildConfig.APP_TOKEN, "f", "", "", mapOf(Pair("display_mode", displayMode))
        ).url

        return createWebActivityIntent(url, color)
    }

    /**
     * Creates a WebActivityIntent Intent with the given [url] and [color].
     */
    fun createWebActivityIntent(url: String, color: IntArray? = null): Intent =
        Intent(
            ApplicationProvider.getApplicationContext(),
            BitLabsOfferwallActivity::class.java
        ).apply {
            putExtra(BUNDLE_KEY_URL, url)
            if (color != null) putExtra(BUNDLE_KEY_HEADER_COLOR, color)
        }
}

fun hasElementWithDataTestId(dataTestId: String) =
    hasElementWithXpath("//*[@data-testid='$dataTestId']")

fun findElementByDataTestId(
    dataTestId: String, selectors: String = ""
): Atom<ElementReference?>? {
    var cssSelector = "[data-testid='$dataTestId']"

    if (selectors.isNotEmpty()) cssSelector += "[$selectors]"

    Log.i(TAG, "findElementByDataTestId: $cssSelector")
    return findElement(Locator.CSS_SELECTOR, cssSelector)
}


fun findElementByText(text: String) = findElement(Locator.XPATH, "//*[text()='$text']")
package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.TestUtils
import ai.bitlabs.sdk.data.model.bitlabs.WebActivityParams
import ai.bitlabs.sdk.util.TAG
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody
import androidx.test.espresso.web.matcher.DomMatchers.hasElementWithXpath
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.model.Atoms.script
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import java.nio.file.Files.getAttribute
import kotlin.math.log

class OfferwallTest {

    private lateinit var url: String
    private lateinit var intent: Intent

    @Before
    fun setUp() {
        url = WebActivityParams(BuildConfig.APP_TOKEN, "f", "", "").url
        intent = TestUtils.createWebActivityIntent(url)
    }

    @Test
    fun closeButton_Clicked_ActivityDestroyed() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(2000)

            onWebView()
                // TODO: use the data-testid when possible
                // .withElement(findElementByDataTestId("title-bar-button-close"))
                .withElement(findElement(Locator.CSS_SELECTOR, "header.title-bar"))
                .withContextualElement(findElement(Locator.TAG_NAME, "button"))
                .perform(webClick())

            Thread.sleep(1000)

            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun newUI_ArbitraryOffersAndStoresExist() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(2000)

            onWebView()
                // Apples - Any Brand
                .check(webContent(hasElementWithDataTestId("offer-144470")))
                // Daiya Frozen Pizza
                .check(webContent(hasElementWithDataTestId("offer-314318")))
                // Walmart
                .check(webContent(hasElementWithDataTestId("merchant-1")))
                // Target
                .check(webContent(hasElementWithDataTestId("merchant-7")))
        }
    }

    @Test
    fun searchBar_WorksAndGoesToPage() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("magic-receipts-search"))
                .withContextualElement(findElement(Locator.TAG_NAME, "input"))
                .perform(webKeys("walm"))

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("search-results"))
                .withContextualElement(findElementByText("Walmart"))
                .check(webMatches(getText(), containsString("Walmart")))

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("magic-receipts-search"))
                .withContextualElement(findElement(Locator.TAG_NAME, "input"))
                .perform(clearElement())

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("magic-receipts-search"))
                .withContextualElement(findElement(Locator.TAG_NAME, "input"))
                .perform(webKeys("appl"))

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("search-results"))
                .withContextualElement(findElementByText("Apples - Any Brand")).perform(webClick())

            Thread.sleep(2000)

            onWebView().check(
                    webMatches(
                        getCurrentUrl(),
                        containsString("/magic-receipts/offer/144470")
                    )
                ).check(webContent(containingTextInBody("Apples - Any Brand")))
                .check(webContent(containingTextInBody("Buying Options")))
        }
    }

    @Test
    fun storeFilter_worksAsExpected() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("open-all-entities-modal"))
                .perform(webClick())

            Thread.sleep(2000)

            onWebView().check(webContent(containingTextInBody("Select Store")))
                .check(webContent(hasElementWithDataTestId("Walmart-filter")))
                .check(webContent(hasElementWithDataTestId("Meijer-filter")))
                .check(webContent(hasElementWithDataTestId("7-Eleven-filter")))
                .check(webContent(hasElementWithDataTestId("Costco Wholesale Corp.-filter")))
                .check(webContent(hasElementWithDataTestId("Home Depot-filter")))
                .withElement(findElementByDataTestId("entity-search"))
                .withContextualElement(findElement(Locator.TAG_NAME, "input"))
                .perform(webKeys("walm"))

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("Walmart-filter")).perform(webClick())


            Thread.sleep(2000)

            onWebView().check(
                    webMatches(
                        getCurrentUrl(),
                        containsString("/magic-receipts/merchant/1")
                    )
                ).check(webContent(containingTextInBody("Deals at Walmart")))
        }
    }

    @Test
    fun cart_addAndRemoveOffer() {
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("offer-144470"))
                .withContextualElement(findElement(Locator.TAG_NAME, "button")).perform(webClick())

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("cart-icon-counter"))
                .check(webMatches(getText(), equalTo("1")))
                .withElement(findElementByDataTestId("cart-icon")).perform(webClick())

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("cart-item-144470"))
                .withContextualElement(findElementByDataTestId("remove-offer-button"))
                .perform(script("arguments[0].click();"))// For some reason, webClick is failing. Use this instead

            Thread.sleep(2000)

            onWebView().withElement(findElementByDataTestId("cart-icon-counter"))
                .check(webMatches(getText(), equalTo("0")))
                .withElement(findElementByDataTestId("cart-icon")).perform(webClick())

            Thread.sleep(2000)

            onWebView().check(webContent(containingTextInBody("Your list is empty")))
                .withElement(findElementByDataTestId("cart-upload-receipt-button", "disabled"))
        }
    }

//    @Test
//    fun uploadImageFlow() {
//        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElementByDataTestId("offer-144470"))
//                .withContextualElement(findElement(Locator.TAG_NAME, "button"))
//                .perform(webClick())
//
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElementByDataTestId("cart-icon"))
//                .perform(webClick())
//
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElementByDataTestId("cart-upload-receipt-button"))
//                .check(webContent(containingTextInBody("Earn up to 37")))
//                .withElement(findElementByDataTestId("cart-upload-receipt-button"))
//                .perform(script("arguments[0].click();"))// For some reason, webClick is failing. Use this instead
//
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElementByText("Continue"))
//                .perform(webClick())
//
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElementByText("Submit Receipt"))
//                .perform(webClick())
//
//            Thread.sleep(2000)
//
//            onWebView()
//                .withElement(findElement(Locator.TAG_NAME, "button"))
////                .withContextualElement(findElementByText("Add Receipt"))
////                .perform(webClick())
//                .perform(script("arguments[0].style.backgroundColor = 'green';"))
//
//            Thread.sleep(2000)
//
//            onView(withText("Gallery")).perform(click())
//        }
//    }
}

private fun hasElementWithDataTestId(dataTestId: String) =
    hasElementWithXpath("//*[@data-testid='$dataTestId']")

private fun findElementByDataTestId(
    dataTestId: String, selectors: String = ""
): Atom<ElementReference?>? {
    var cssSelector = "[data-testid='$dataTestId']"

    if (selectors.isNotEmpty()) cssSelector += "[$selectors]"

    Log.i(TAG, "findElementByDataTestId: $cssSelector")
    return findElement(Locator.CSS_SELECTOR, cssSelector)
}


private fun findElementByText(text: String) = findElement(Locator.XPATH, "//*[text()='$text']")

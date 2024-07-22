package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Test

private const val TOKEN = BuildConfig.APP_TOKEN
private const val UID = "diffindocongress"

class WebActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun urlExtra_No_BUNDLE_KEY_URL_DestroyActivity() {
        ActivityScenario.launch(WebActivity::class.java).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_EmptyString_DestroyActivity() {
        val intent = TestUtils.createWebActivityIntent("")

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_NotString_DestroyActivity() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, 123)
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_CorrectURLString_CreateActivity() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun colorExtra_No_BUNDLE_KEY_COLOR_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<WebActivity>(intent).use {
            awaitView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_Empty_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url, intArrayOf())

        ActivityScenario.launch<WebActivity>(intent).use {
            awaitView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_NotIntArrayOf_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            putExtra(BUNDLE_KEY_COLOR, 123)
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            awaitView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_CorrectIntArray_CorrectToolbarBackground() {
        val colors = intArrayOf(123, 123)
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url, colors)

        ActivityScenario.launch<WebActivity>(intent).use {
            awaitView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(colors)
            }
        }
    }

    @Test
    fun toolbar_PageIsBitlabsOfferwall_IsNotDisplayed() {
        val url = "https://web.bitlabs.ai"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun toolbar_PageIsNotOfferwall_IsDisplayed() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<WebActivity>(intent).use {
            awaitView(withId(R.id.toolbar_bitlabs)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onBackPressed_PageIsNotOfferwall_ShowLeaveSurveyDialog() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(isRoot()).perform(waitForFocus())
            awaitView(isRoot()).perform(pressBack())
            awaitView(withId(androidx.appcompat.R.id.alertTitle)).inRoot(isDialog())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun leaveSurveyDialog_AnyOptionClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)
        val surveyStartHookEventMessage = """
            {
                "type": "hook",
                "name": "offerwall-surveys:survey.start",
                "args": [{clickId: "123", link: ""}]
            }
        """.trimIndent()
        val jsCode = "window.postMessage($surveyStartHookEventMessage, '*');"

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<WebActivity>(intent).use {
                awaitView(withId(R.id.wv_bitlabs))

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                awaitView(withId(R.id.wv_bitlabs))

                onView(isRoot()).perform(waitForFocus())
                awaitView(isRoot()).perform(pressBack())

                awaitView(withText(R.string.leave_reason_other)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 1) { BitLabs.leaveSurvey(any(), any()) }

                awaitView(withId(R.id.wv_bitlabs))

                onView(isRoot()).perform(waitForFocus())
                awaitView(isRoot()).perform(pressBack())

                awaitView(withText(R.string.leave_reason_sensitive)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 2) { BitLabs.leaveSurvey(any(), any()) }

                awaitView(withId(R.id.wv_bitlabs))

                onView(isRoot()).perform(waitForFocus())
                awaitView(isRoot()).perform(pressBack())

                awaitView(withText(R.string.leave_reason_technical)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 3) { BitLabs.leaveSurvey(any(), any()) }

                awaitView(withId(R.id.wv_bitlabs))

                onView(isRoot()).perform(waitForFocus())
                awaitView(isRoot()).perform(pressBack())

                awaitView(withText(R.string.leave_reason_uninteresting)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 4) { BitLabs.leaveSurvey(any(), any()) }

                awaitView(withId(R.id.wv_bitlabs))

                onView(isRoot()).perform(waitForFocus())
                awaitView(isRoot()).perform(pressBack())

                awaitView(withText(R.string.leave_reason_too_long)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 5) { BitLabs.leaveSurvey(any(), any()) }
            }
        }
    }
}

/**
 * Utility object for creating common Intents and Bundles for tests.
 */
object TestUtils {
    /**
     * Creates a WebActivityIntent Intent with the given [url] and [color].
     */
    fun createWebActivityIntent(url: String, color: IntArray? = null): Intent =
        Intent(ApplicationProvider.getApplicationContext(), WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            if (color != null) putExtra(BUNDLE_KEY_COLOR, color)
        }
}

private fun awaitView(viewMatcher: Matcher<View>, timeoutMillis: Long = 5000): ViewInteraction {
    val endTime = System.currentTimeMillis() + timeoutMillis
    while (System.currentTimeMillis() < endTime) {
        try {
            val match = onView(viewMatcher).check(matches(isDisplayed()))
            return match
        } catch (e: NoMatchingViewException) {
            Thread.sleep(50)
        } catch (e: AssertionError) {
            Thread.sleep(50)
        }
    }
    throw AssertionError("View not displayed: $viewMatcher")
}

fun waitForFocus(timeout: Long = 10000): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Wait for the root view to gain window focus for up to $timeout milliseconds."
        }

        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun perform(uiController: UiController?, view: View?) {
            val endTime = System.currentTimeMillis() + timeout
            while (System.currentTimeMillis() < endTime) {
                if (view?.hasWindowFocus() == true) {
                    return
                }
                uiController?.loopMainThreadForAtLeast(50)
            }
            throw AssertionError("Root view did not gain window focus within $timeout milliseconds.")
        }
    }
}

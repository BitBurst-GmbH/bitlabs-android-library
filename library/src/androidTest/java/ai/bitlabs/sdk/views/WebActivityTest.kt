package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.util.BUNDLE_KEY_HEADER_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
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
import org.junit.Test

private val surveyStartHookEventMessage = """
            {
                "type": "hook",
                "name": "offerwall-surveys:survey.start",
                "args": [{clickId: "123", link: ""}]
            }
        """.trimIndent()
private val jsCode = "window.postMessage($surveyStartHookEventMessage, '*');"

class WebActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun urlExtra_No_BUNDLE_KEY_URL_DestroyActivity() {
        ActivityScenario.launch(BitLabsOfferwallActivity::class.java).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_EmptyString_DestroyActivity() {
        val intent = TestUtils.createWebActivityIntent("")

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_NotString_DestroyActivity() {
        val intent = Intent(context, BitLabsOfferwallActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, 123)
        }

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_CorrectURLString_CreateActivity() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun colorExtra_No_BUNDLE_KEY_COLOR_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            it.onActivity { activity ->
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar_bitlabs)
                val gradient = toolbar.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_Empty_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url, intArrayOf())

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            it.onActivity { activity ->
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar_bitlabs)
                val gradient = toolbar.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_NotIntArrayOf_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val url = "https://www.google.com"
        val intent = Intent(context, BitLabsOfferwallActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            putExtra(BUNDLE_KEY_HEADER_COLOR, 123)
        }

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            it.onActivity { activity ->
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar_bitlabs)
                val gradient = toolbar.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_CorrectIntArray_CorrectToolbarBackground() {
        val colors = intArrayOf(123, 123)
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url, colors)

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            it.onActivity { activity ->
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar_bitlabs)
                val gradient = toolbar.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(colors)
            }
        }
    }

    @Test
    fun toolbar_SurveyStartEvent_IsDisplayed() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)

            it.onActivity { activity ->
                activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
            }

            Thread.sleep(1000)
            onView(withId(R.id.toolbar_bitlabs)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onBackPressed_SurveyStartEvent_ShowLeaveSurveyDialog() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)

            it.onActivity { activity ->
                activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
            }

            Thread.sleep(500)
            onView(isRoot()).perform(pressBack())

            Thread.sleep(500)
            onView(withId(androidx.appcompat.R.id.alertTitle)).inRoot(isDialog())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun leaveSurveyDialog_TechnicalReasonClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)
                onView(isRoot()).perform(pressBack())

                Thread.sleep(500)
                onView(withText(R.string.leave_reason_technical)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify { BitLabs.leaveSurvey(any(), any()) }
            }
        }
    }

    @Test
    fun leaveSurveyDialog_OtherReasonClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)
                onView(isRoot()).perform(pressBack())

                Thread.sleep(500)
                onView(withText(R.string.leave_reason_other)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify { BitLabs.leaveSurvey(any(), any()) }
            }
        }
    }

    @Test
    fun leaveSurveyDialog_TooLongReasonClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)
                onView(isRoot()).perform(pressBack())

                Thread.sleep(500)
                onView(withText(R.string.leave_reason_too_long)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify { BitLabs.leaveSurvey(any(), any()) }
            }
        }
    }

    @Test
    fun leaveSurveyDialog_SensitiveReasonClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)
                onView(isRoot()).perform(pressBack())

                Thread.sleep(500)
                onView(withText(R.string.leave_reason_sensitive)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify { BitLabs.leaveSurvey(any(), any()) }
            }
        }
    }

    @Test
    fun leaveSurveyDialog_UninterestingReasonClicked_LeaveSurveyCalled() {
        val url = "https://www.google.com"
        val intent = TestUtils.createWebActivityIntent(url)

        mockkObject(BitLabs) {
            every { BitLabs.leaveSurvey(any(), any()) } returns Unit

            ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)
                onView(isRoot()).perform(pressBack())

                Thread.sleep(500)
                onView(withText(R.string.leave_reason_uninteresting)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify { BitLabs.leaveSurvey(any(), any()) }
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
        Intent(ApplicationProvider.getApplicationContext(), BitLabsOfferwallActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            if (color != null) putExtra(BUNDLE_KEY_HEADER_COLOR, color)
        }
}
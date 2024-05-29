package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import com.google.common.base.Verify.verify
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test

private const val TOKEN = BuildConfig.APP_TOKEN
private const val UID = "diffindocongress"

class WebActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun paramsExtra_No_BUNDLE_KEY_PARAMS_DestroyActivity() {
        ActivityScenario.launch(WebActivity::class.java).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun paramsExtra_BUNDLE_KEY_PARAMS_Empty_DestroyActivity() {
        val intent = TestUtils.createWebActivityIntent(Bundle())

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun paramsExtra_BUNDLE_KEY_PARAMS_NotBundle_DestroyActivity() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_PARAMS, 123)
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun paramsExtra_BUNDLE_KEY_PARAMS_CorrectParamsBundle_CreateActivity() {
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = TestUtils.createWebActivityIntent(params)

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun colorExtra_No_BUNDLE_KEY_COLOR_WhiteToolbarBackground() {
        // Create a WebActivity with non-OfferWall URL to show the toolbar
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = TestUtils.createWebActivityIntent(params)

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_Empty_WhiteToolbarBackground() {
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = TestUtils.createWebActivityIntent(params, intArrayOf())

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_NotIntArrayOf_WhiteToolbarBackground() {
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_PARAMS, params)
            putExtra(BUNDLE_KEY_COLOR, 123)
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(intArrayOf(Color.WHITE, Color.WHITE))
            }
        }
    }

    @Test
    fun colorExtra_BUNDLE_KEY_COLOR_CorrectIntArray_CorrectToolbarBackground() {
        val colors = intArrayOf(123, 123)
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = TestUtils.createWebActivityIntent(params, colors)

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(colors)
            }
        }
    }

    @Test
    fun toolbar_PageIsBitlabsOfferwall_IsNotDisplayed() {
        val params = TestUtils.createWebActivityParamsBundle()
        val intent = TestUtils.createWebActivityIntent(params)

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun toolbar_PageIsNotOfferwall_IsDisplayed() {
        val params = TestUtils.createWebActivityParamsBundle("https://www.google.com")
        val intent = TestUtils.createWebActivityIntent(params)

        ActivityScenario.launch<WebActivity>(intent).use {
            Thread.sleep(500)
            onView(withId(R.id.toolbar_bitlabs)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onBackPressed_PageIsNotOfferwall_ShowLeaveSurveyDialog() {
        val params = TestUtils.createWebActivityParamsBundle("https://www.google.com")
        val intent = TestUtils.createWebActivityIntent(params)

        ActivityScenario.launch<WebActivity>(intent).use {
            Thread.sleep(500)
            onView(isRoot()).perform(pressBack())
            onView(withId(androidx.appcompat.R.id.alertTitle)).inRoot(isDialog())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun leaveSurveyDialog_AnyOptionClicked_LeaveSurveyCalled() {
        val params = TestUtils.createWebActivityParamsBundle("https://www.google.com")
        val intent = TestUtils.createWebActivityIntent(params)
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
                Thread.sleep(500)

                it.onActivity { activity ->
                    activity.findViewById<WebView>(R.id.wv_bitlabs).evaluateJavascript(jsCode) {}
                }

                Thread.sleep(500)

                onView(isRoot()).perform(pressBack())

                onView(withText(R.string.leave_reason_other)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 1) { BitLabs.leaveSurvey(any(), any()) }

                Thread.sleep(400)

                onView(isRoot()).perform(pressBack())

                onView(withText(R.string.leave_reason_sensitive)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 2) { BitLabs.leaveSurvey(any(), any()) }

                Thread.sleep(400)

                onView(isRoot()).perform(pressBack())

                onView(withText(R.string.leave_reason_technical)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 3) { BitLabs.leaveSurvey(any(), any()) }

                Thread.sleep(400)

                onView(isRoot()).perform(pressBack())

                onView(withText(R.string.leave_reason_uninteresting)).inRoot(isDialog())
                    .check(matches(isDisplayed())).perform(click())

                verify(exactly = 4) { BitLabs.leaveSurvey(any(), any()) }

                Thread.sleep(400)

                onView(isRoot()).perform(pressBack())

                onView(withText(R.string.leave_reason_too_long)).inRoot(isDialog())
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
     * Creates a WebActivityIntent Intent with the given [params] and [color].
     */
    fun createWebActivityIntent(params: Bundle, color: IntArray? = null): Intent =
        Intent(ApplicationProvider.getApplicationContext(), WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_PARAMS, params)
            if (color != null) putExtra(BUNDLE_KEY_COLOR, color)
        }

    /**
     * Creates a common [WebActivityParams] Bundle.
     */
    fun createWebActivityParamsBundle(url: String = "https://web.bitlabs.ai"): Bundle =
        Bundle().apply {
            putString("url", url)
            putString("token", TOKEN)
            putString("uid", UID)
        }
}

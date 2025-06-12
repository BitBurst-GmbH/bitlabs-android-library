package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.offerwall.components.webview.BLWebView
import ai.bitlabs.sdk.offerwall.components.webview.BLWebViewViewModel
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_UID
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
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

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: BLWebViewViewModel

    @Before
    fun setUp() {
        // Initialize the ViewModel with mock data
        viewModel = mockk(relaxed = true)

        every { viewModel.leaveSurvey(any()) } returns Unit
        every { viewModel.onSurveyReward(any()) } returns Unit
        every { viewModel.onOfferwallClosed() } returns Unit

        every { viewModel.headerColors } returns mutableStateOf(
            intArrayOf(
                Color.Blue.toArgb(),
                Color.Green.toArgb()
            )
        )
        every { viewModel.backgroundColors } returns mutableStateOf(
            intArrayOf(
                Color.Green.toArgb(),
                Color.Green.toArgb()
            )
        )
    }

    @Test
    fun urlExtra_No_BUNDLE_KEY_URL_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .token("valid_token")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_EmptyString_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .token("valid_token")
            .url("")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_NotString_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .token("valid_token")
            .intURL(123)
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_CorrectURLString_CreateActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .token("valid_token")
            .url("https://www.google.com")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun tokenExtra_No_BUNDLE_KEY_TOKEN_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .url("https://www.google.com")
            .mock()

        // Create a WebActivity without the token extra
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun tokenExtra_BUNDLE_KEY_TOKEN_NotString_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .url("https://www.google.com")
            .intToken(123)
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun tokenExtra_BUNDLE_KEY_TOKEN_CorrectString_CreateActivity() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .url("https://www.google.com")
            .token("valid_token")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun uidExtra_No_BUNDLE_KEY_UID_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .token("valid_token")
            .url("https://www.google.com")
            .mock()

        // Create a WebActivity without the UID extra
        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun uidExtra_BUNDLE_KEY_UID_NotString_DestroyActivity() {
        val intent = MockWebActivityIntent(context)
            .token("valid_token")
            .url("https://www.google.com")
            .intUid(123)
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun uidExtra_BUNDLE_KEY_UID_CorrectString_CreateActivity() {
        val intent = MockWebActivityIntent(context)
            .token("valid_token")
            .url("https://www.google.com")
            .uid("valid_uid")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun toolbar_SurveyStartEvent_IsDisplayed() {
        val intent = MockWebActivityIntent(context)
            .uid("valid_uid")
            .token("valid_token")
            .url("https://www.google.com")
            .mock()

        ActivityScenario.launch<BitLabsOfferwallActivity>(intent).use {
            Thread.sleep(500)

            it.onActivity {
                it.findViewById<WebView>(R.id.bl_webview)
                    .evaluateJavascript(jsCode) {}
            }

            Thread.sleep(500)

            // Check if the toolbar is displayed
            rule.onNodeWithTag("BLTopBar").assertExists()
        }
    }

    @Test
    fun onBackPressed_SurveyStartEvent_ShowLeaveSurveyDialog() {
        rule.setContent { BLWebView(viewModel, "https://www.google.com") }

        Thread.sleep(500)

        rule.runOnUiThread {
            val webView = rule.activity.findViewById<WebView>(R.id.bl_webview)
            webView.evaluateJavascript(jsCode) {}
        }

        Thread.sleep(500)

        // Simulate back press
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }

        Thread.sleep(500)

        // Check if the leave survey dialog is displayed
        rule.onNodeWithTag("BLLeaveSurveyDialog").assertExists()
    }

    private fun testLeaveReasonClicked(reason: String) {
        rule.setContent { BLWebView(viewModel, "https://www.google.com") }

        Thread.sleep(500)

        rule.runOnUiThread {
            val webView = rule.activity.findViewById<WebView>(R.id.bl_webview)
            webView.evaluateJavascript(jsCode) {}
        }

        Thread.sleep(500)

        // Simulate back press
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }

        Thread.sleep(500)

        rule.onNodeWithText(reason)
            .assertExists()
            .performClick()

        Thread.sleep(500)

        verify { viewModel.leaveSurvey(any()) }
    }

    @Test
    fun leaveSurveyDialog_TechnicalReasonClicked_LeaveSurveyCalled() {
        val technicalReason = context.getString(R.string.leave_reason_technical)
        testLeaveReasonClicked(technicalReason)
    }

    @Test
    fun leaveSurveyDialog_OtherReasonClicked_LeaveSurveyCalled() {
        val otherReason = context.getString(R.string.leave_reason_other)
        testLeaveReasonClicked(otherReason)
    }

    @Test
    fun leaveSurveyDialog_TooLongReasonClicked_LeaveSurveyCalled() {
        val tooLongReason = context.getString(R.string.leave_reason_too_long)
        testLeaveReasonClicked(tooLongReason)
    }

    @Test
    fun leaveSurveyDialog_SensitiveReasonClicked_LeaveSurveyCalled() {
        val sensitiveReason = context.getString(R.string.leave_reason_sensitive)
        testLeaveReasonClicked(sensitiveReason)
    }

    @Test
    fun leaveSurveyDialog_UninterestingReasonClicked_LeaveSurveyCalled() {
        val uninterestingReason = context.getString(R.string.leave_reason_uninteresting)
        testLeaveReasonClicked(uninterestingReason)
    }
}

class MockWebActivityIntent(context: Context) {
    private val intent = Intent(context, BitLabsOfferwallActivity::class.java)

    fun token(token: String?) = apply {
        intent.putExtra(BUNDLE_KEY_TOKEN, token)
    }

    fun uid(uid: String?) = apply {
        intent.putExtra(BUNDLE_KEY_UID, uid)
    }

    fun url(url: String?) = apply {
        intent.putExtra(BUNDLE_KEY_URL, url)
    }

    fun intURL(url: Int) = apply {
        intent.putExtra(BUNDLE_KEY_URL, url)
    }

    fun intToken(token: Int) = apply {
        intent.putExtra(BUNDLE_KEY_TOKEN, token)
    }

    fun intUid(uid: Int) = apply {
        intent.putExtra(BUNDLE_KEY_UID, uid)
    }

    fun mock() = intent
}
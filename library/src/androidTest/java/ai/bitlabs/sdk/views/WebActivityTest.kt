package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WebActivityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun urlExtra_No_BUNDLE_KEY_URL_DestroyActivity() {
        ActivityScenario.launch(WebActivity::class.java).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_Empty_DestroyActivity() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "")
        }

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
    fun urlExtra_BUNDLE_KEY_URL_NotCorrectUrl_DestroyActivity() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "String not URL")
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun urlExtra_BUNDLE_KEY_URL_CorrectFormUrl_CreateActivity() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "https://www.google.com")
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun colorExtra_No_BUNDLE_KEY_COLOR_WhiteToolbarBackground() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "https://www.google.com")
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
    fun colorExtra_BUNDLE_KEY_COLOR_Empty_WhiteToolbarBackground() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "https://www.google.com")
            putExtra(BUNDLE_KEY_COLOR, intArrayOf())
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
    fun colorExtra_BUNDLE_KEY_COLOR_NotIntArrayOf_WhiteToolbarBackground() {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "https://www.google.com")
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

        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, "https://www.google.com")
            putExtra(BUNDLE_KEY_COLOR, colors)
        }

        ActivityScenario.launch<WebActivity>(intent).use {
            onView(withId(R.id.toolbar_bitlabs)).check { view, _ ->
                assertThat(view).isInstanceOf(Toolbar::class.java)
                val gradient = view.background as GradientDrawable
                assertThat(gradient.colors).isEqualTo(colors)
            }
        }
    }
}
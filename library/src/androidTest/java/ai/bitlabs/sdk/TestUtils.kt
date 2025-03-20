package ai.bitlabs.sdk

import ai.bitlabs.sdk.util.BUNDLE_KEY_HEADER_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.views.BitLabsOfferwallActivity
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

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
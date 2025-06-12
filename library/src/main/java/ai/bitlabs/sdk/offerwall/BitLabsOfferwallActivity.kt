package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.offerwall.components.webview.BLViewModelFactory
import ai.bitlabs.sdk.offerwall.components.webview.BLWebView
import ai.bitlabs.sdk.offerwall.components.webview.BLWebViewViewModel
import ai.bitlabs.sdk.offerwall.util.OfferwallListenerManager
import ai.bitlabs.sdk.util.BUNDLE_KEY_LISTENER_ID
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_UID
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

/**
 * The [Activity][androidx.appcompat.app.AppCompatActivity] that will provide a [android.webkit.WebView] to launch the OfferWall.
 */
internal class BitLabsOfferwallActivity : AppCompatActivity() {

    private var uid = ""
    private var token = ""
    private var listenerId = 0
    private lateinit var url: String

    private val blViewModel: BLWebViewViewModel by viewModels {
        BLViewModelFactory(token, uid, listenerId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            getDataFromIntent()
        } catch (e: IllegalArgumentException) {
            SentryManager.captureException(token, uid, e)
            Log.e(TAG, e.message.toString())
            finish()
            return
        }

        setContent { BLWebView(blViewModel, url) }
    }

    override fun onDestroy() {
        OfferwallListenerManager.unregisterListeners(listenerId)
        super.onDestroy()
    }

    private fun getDataFromIntent() {
        token = intent.getStringExtra(BUNDLE_KEY_TOKEN) ?: run {
            throw IllegalArgumentException("BitLabsOfferwallActivity - Token is null!")
        }

        uid = intent.getStringExtra(BUNDLE_KEY_UID) ?: run {
            throw IllegalArgumentException("BitLabsOfferwallActivity - UID is null!")
        }

        url = intent.getStringExtra(BUNDLE_KEY_URL).takeIf { URLUtil.isValidUrl(it) } ?: run {
            throw IllegalArgumentException("BitLabsOfferwallActivity - Invalid url!")
        }

        listenerId = intent.getIntExtra(BUNDLE_KEY_LISTENER_ID, -1)
    }
}
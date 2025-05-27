package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.util.BUNDLE_KEY_LISTENER_ID
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extensions.setup
import ai.bitlabs.sdk.util.setQRCodeBitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

/**
 * The [Activity][androidx.appcompat.app.AppCompatActivity] that will provide a [android.webkit.WebView] to launch the OfferWall.
 */
internal class BitLabsOfferwallActivity : AppCompatActivity() {

    private var webView: WebView? = null

    private var token = ""
    private var listenerId = 0
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            getDataFromIntent()
        } catch (e: IllegalArgumentException) {
            SentryManager.captureException(e)
            Log.e(TAG, e.message.toString())
            finish()
            return
        }

        setContent { BLWebView(token, url, listenerId) }

        bindUI()
    }

    override fun onDestroy() {
        OfferwallListenerManager.unregisterListeners(listenerId)
        super.onDestroy()
    }

    private fun getDataFromIntent() {
        url = intent.getStringExtra(BUNDLE_KEY_URL).takeIf { URLUtil.isValidUrl(it) } ?: run {
            throw IllegalArgumentException("BitLabsOfferwallActivity - Invalid url!")
        }

        token = intent.getStringExtra(BUNDLE_KEY_TOKEN) ?: run {
            throw IllegalArgumentException("BitLabsOfferwallActivity - Token is null!")
        }

        listenerId = intent.getIntExtra(BUNDLE_KEY_LISTENER_ID, -1)
    }

    private fun bindUI() {
        webView?.setup(
            { },
            { },
            { },
            { error, date, errUrl ->
                val errorInfo =
                    "code: ${error?.getStatusCode()}, description: ${error?.getDescription()}"

                val errorStr =
                    "{ ${errorInfo}, uid: UserId, url: $errUrl, date: $date }".toByteArray()
                        .let { Base64.encodeToString(it, Base64.DEFAULT) }

                findViewById<LinearLayout>(R.id.ll_qr_code_bitlabs)?.let {
                    it.visibility = View.VISIBLE
                    (it.children.last() as? TextView)?.text =
                        getString(R.string.error_id, errorStr).trim()
                    (it.children.first() as? ImageView)?.setQRCodeBitmap(errorStr)
                }
            }
        )
    }
}
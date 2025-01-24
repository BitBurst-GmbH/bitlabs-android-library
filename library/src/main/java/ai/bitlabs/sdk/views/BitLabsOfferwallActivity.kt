package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.getLuminance
import ai.bitlabs.sdk.util.setQRCodeBitmap
import ai.bitlabs.sdk.util.extensions.setup
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children


/**
 * The [Activity][AppCompatActivity] that will provide a [WebView] to launch the OfferWall.
 */
internal class BitLabsOfferwallActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null

    private lateinit var url: String

    private var totalReward: Float = 0.0F
    private var clickId: String? = null
    private var colors = intArrayOf(Color.WHITE, Color.WHITE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offerwall_bitlabs)

        try {
            getDataFromIntent()
        } catch (e: IllegalArgumentException) {
            SentryManager.captureException(e)
            Log.e(TAG, e.message.toString())
            finish()
            return
        }

        bindUI()

        if (savedInstanceState == null) webView?.loadUrl(url)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (toolbar?.visibility == View.VISIBLE) {
                    showLeaveSurveyAlertDialog()
                    return
                }

                if (webView?.canGoBack() == true) {
                    webView?.goBack()
                    return
                }

                if (isEnabled) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView?.restoreState(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) showLeaveSurveyAlertDialog()
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        BitLabs.onRewardListener?.onReward(totalReward)
        super.onStop()
    }

    private fun getDataFromIntent() {
        url = intent.getStringExtra(BUNDLE_KEY_URL)
            .takeIf { URLUtil.isValidUrl(it) } ?: run {
            throw IllegalArgumentException("WebActivity - Invalid url!")
        }

        colors = intent.getIntArrayExtra(BUNDLE_KEY_COLOR)?.takeIf { it.isNotEmpty() } ?: colors
    }

    private fun bindUI() {
        val isColorBright =
            getLuminance(colors.first()) > 0.729 * 255 || getLuminance(colors.last()) > 0.729 * 255

        toolbar = findViewById(R.id.toolbar_bitlabs)
        (toolbar?.background?.mutate() as? GradientDrawable)?.let {
            it.colors = colors
            it.cornerRadius = 0F
        }

        toolbar?.setTitleTextColor(if (isColorBright) Color.BLACK else Color.WHITE)
        toolbar?.navigationIcon?.let {
            DrawableCompat.setTint(it, if (isColorBright) Color.BLACK else Color.WHITE)
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toggleToolbar(false)

        webView = findViewById(R.id.wv_bitlabs)
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        webView?.setup(
            { reward -> totalReward += reward },
            { clk -> clickId = clk },
            { shouldShowToolbar -> toggleToolbar(shouldShowToolbar) }
        ) { error, date, errUrl ->
            val errorInfo =
                "code: ${error?.getStatusCode()}, description: ${error?.getDescription()}"

            val errorStr = "{ ${errorInfo}, uid: UserId, url: $errUrl, date: $date }".toByteArray()
                .let { Base64.encodeToString(it, Base64.DEFAULT) }

            findViewById<LinearLayout>(R.id.ll_qr_code_bitlabs)?.let {
                it.visibility = View.VISIBLE
                (it.children.last() as? TextView)?.text =
                    getString(R.string.error_id, errorStr).trim()
                (it.children.first() as? ImageView)?.setQRCodeBitmap(errorStr)
            }
        }
    }

    /** Shows or hides some UI elements according to whether [shouldShowToolbar] is `true` or `false`. */
    private fun toggleToolbar(shouldShowToolbar: Boolean) {
        toolbar?.visibility = if (shouldShowToolbar) View.VISIBLE else View.GONE

        webView?.isScrollbarFadingEnabled = shouldShowToolbar
        webView?.settings?.run {
            setSupportZoom(shouldShowToolbar)
            builtInZoomControls = shouldShowToolbar
        }
    }

    /** Shows the Alert Dialog that lets the user choose a reason why they want to leave the survey. */
    private fun showLeaveSurveyAlertDialog() {
        val options = arrayOf("SENSITIVE", "UNINTERESTING", "TECHNICAL", "TOO_LONG", "OTHER")
        val optionsDisplay = arrayOf(
            getString(R.string.leave_reason_sensitive),
            getString(R.string.leave_reason_uninteresting),
            getString(R.string.leave_reason_technical),
            getString(R.string.leave_reason_too_long),
            getString(R.string.leave_reason_other)
        )
        AlertDialog.Builder(this).setTitle(getString(R.string.leave_dialog_title))
            .setItems(optionsDisplay) { _, which -> leaveSurvey(options[which]) }
            .setNegativeButton(getString(R.string.leave_dialog_continue)) { _, _ -> }.show()
    }

    /** Loads the OfferWall page and sends the [reason] to the API */
    private fun leaveSurvey(reason: String) {
        findViewById<LinearLayout>(R.id.ll_qr_code_bitlabs)?.visibility = View.GONE
        toggleToolbar(false)
        webView?.evaluateJavascript(" window.history.go(-window.history.length + 1);", null);

        if (clickId != null) BitLabs.leaveSurvey(clickId!!, reason)
    }
}

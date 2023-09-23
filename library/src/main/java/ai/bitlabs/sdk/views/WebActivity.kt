package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.getLuminance
import ai.bitlabs.sdk.util.setQRCodeBitmap
import ai.bitlabs.sdk.util.setup
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children


/**
 * The [Activity][AppCompatActivity] that will provide a [WebView] to launch the OfferWall.
 */
internal class WebActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null

    private lateinit var url: String

    private var reward: Float = 0.0F
    private var clickId: String? = null
    private var colors = intArrayOf(Color.WHITE, Color.WHITE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        url = intent.getStringExtra(BUNDLE_KEY_URL)
            ?.takeIf { URLUtil.isValidUrl(it) } ?: run {
            Log.e(TAG, "WebActivity - No bundle data found!")
            finish()
            return
        }

        colors = intent.getIntArrayExtra(BUNDLE_KEY_COLOR)?.takeIf { it.isNotEmpty() } ?: colors

        bindUI()

        if (savedInstanceState == null) webView?.loadUrl(url)
    }

    override fun onBackPressed() {
        if (toolbar?.visibility == View.VISIBLE) showLeaveSurveyAlertDialog()
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
        BitLabs.onRewardListener?.onReward(reward)
        super.onStop()
    }

    /** A function to configure all UI elements and the logic behind them, if any. */
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

        toggleToolbar(true)

        webView = findViewById(R.id.wv_bitlabs)
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        webView?.setup(this, { isPageOfferWall, url ->
            if (url.contains("/close")) {
                finish()
                return@setup
            }

            Log.i(TAG, "bindUI: $url")
            if (isPageOfferWall) {
                if (url.contains("/survey-complete") || url.contains("/survey-screenout") || url.contains(
                        "/start-bonus"
                    )
                ) Uri.parse(url).getQueryParameter("val")?.let { reward += it.toFloat() }
            } else {
                Uri.parse(url).getQueryParameter("clk")?.let { clickId = it }
            }
            toggleToolbar(isPageOfferWall)
        }, { date ->
            val error = "{ uid: UserId, date: $date }".toByteArray()
                .let { Base64.encodeToString(it, Base64.DEFAULT) }

            findViewById<LinearLayout>(R.id.ll_qr_code)?.let {
                it.visibility = View.VISIBLE
                (it.children.last() as? TextView)?.text = getString(R.string.error_id, error).trim()
                (it.children.first() as? ImageView)?.setQRCodeBitmap(error)
            }
        })
    }

    /** Shows or hides some UI elements according to whether [isPageOfferWall] is `true` or `false`. */
    private fun toggleToolbar(isPageOfferWall: Boolean) {
        toolbar?.visibility = if (isPageOfferWall) View.GONE else View.VISIBLE

        webView?.isScrollbarFadingEnabled = !isPageOfferWall
        webView?.settings?.run {
            setSupportZoom(!isPageOfferWall)
            builtInZoomControls = !isPageOfferWall
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
        findViewById<LinearLayout>(R.id.ll_qr_code)?.visibility = View.GONE
        toggleToolbar(true)
        webView?.loadUrl(url)

        if (clickId != null) BitLabs.leaveSurvey(clickId!!, reason)
    }
}

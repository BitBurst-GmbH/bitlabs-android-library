package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.util.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat

/**
 * The [Activity][AppCompatActivity] that will provide a [WebView] to launch the OfferWall.
 */
internal class WebActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null
    private var closeButton: ImageView? = null

    private lateinit var url: String

    private var clickId: String? = null
    private var colors = intArrayOf(0, 0)

    private var reward: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        url = intent.getStringExtra(BUNDLE_KEY_PARAMS) ?: run {
            Log.e(TAG, "WebActivity - No bundle data found!")
            finish()
            return
        }

        colors = intent.getIntArrayExtra(BUNDLE_KEY_COLOR) ?: colors

        bindUI()

        if (savedInstanceState == null)
            webView?.loadUrl("http://http.badssl.com/")
    }

    override fun onBackPressed() {
        if (toolbar?.visibility == View.VISIBLE)
            showLeaveSurveyAlertDialog()
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

        closeButton = findViewById(R.id.iv_close_bitlabs)
        closeButton?.setOnClickListener { finish() }
        closeButton?.run {
            DrawableCompat.setTint(drawable, if (isColorBright) Color.BLACK else Color.WHITE)
        }
        toggleToolbar(true)

        webView = findViewById(R.id.wv_bitlabs)
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        webView?.setup(this) { isPageOfferWall, url ->
            Log.i(TAG, "bindUI: $url")
            if (isPageOfferWall) {
                if (url.contains("/survey-complete")
                    || url.contains("/survey-screenout")
                    || url.contains("/start-bonus")
                )
                    Uri.parse(url).getQueryParameter("val")?.let { reward += it.toFloat() }
            } else {
                Uri.parse(url).getQueryParameter("clk")?.let { clickId = it }
            }
            toggleToolbar(isPageOfferWall)
        }
    }

    /** Shows or hides some UI elements according to whether [isPageOfferWall] is `true` or `false`. */
    private fun toggleToolbar(isPageOfferWall: Boolean) {
        toolbar?.visibility = if (isPageOfferWall) View.GONE else View.VISIBLE
        closeButton?.visibility = if (isPageOfferWall) View.VISIBLE else View.GONE
        (if (isPageOfferWall) closeButton else toolbar)?.bringToFront()

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
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.leave_dialog_title))
            .setItems(optionsDisplay) { _, which -> leaveSurvey(options[which]) }
            .setNegativeButton(getString(R.string.leave_dialog_continue)) { _, _ -> }
            .show()
    }

    /** Loads the OfferWall page and sends the [reason] to the API */
    private fun leaveSurvey(reason: String) {
        toggleToolbar(true)
        webView?.loadUrl(url)

        if (clickId != null) BitLabs.leaveSurvey(clickId!!, reason)
    }
}

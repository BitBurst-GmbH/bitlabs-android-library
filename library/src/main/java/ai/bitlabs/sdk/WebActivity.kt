package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.TAG
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * The [Activity][AppCompatActivity] that will provide a [WebView] to launch the OfferWall.
 */
internal class WebActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null
    private var closeButton: ImageView? = null

    private lateinit var params: WebActivityParams

    private var networkId: String? = null
    private var surveyId: String? = null

    private var reward: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        params = intent.extras?.getSerializable(BUNDLE_KEY_PARAMS) as? WebActivityParams ?: run {
            Log.e(TAG, "WebActivity - No bundle data found!")
            finish()
            return
        }

        bindUI()

        if (savedInstanceState == null)
            webView?.loadUrl(params.url)
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
        if (item.itemId == android.R.id.home)
            showLeaveSurveyAlertDialog()
        return super.onOptionsItemSelected(item)
    }

    /** A function to configure all UI elements and the logic behind them, if any. */
    private fun bindUI() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        closeButton = findViewById(R.id.close)
        closeButton?.setOnClickListener { finish() }
        toggleToolbar(true)

        webView = findViewById(R.id.web)
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        webView?.setup(this) { isPageOfferWall, url ->
            if (isPageOfferWall) {
                if (url.contains("survey/complete") || url.contains("survey/screenout"))
                    Uri.parse(url).getQueryParameter("val")?.let { reward += it.toFloat() }
            } else {
                Regex("/networks/(\\d+)/surveys/(\\d+)").find(url)?.let { match ->
                    val (networkId, surveyId) = match.destructured
                    this.networkId = networkId
                    this.surveyId = surveyId
                }
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

    /** Loads the OfferWall page and triggers the [WebActivityParams.leaveSurveyListener] */
    private fun leaveSurvey(reason: String) {
        toggleToolbar(true)
        webView?.loadUrl(params.url)

        if (networkId != null && surveyId != null)
            params.leaveSurveyListener.leaveSurvey(networkId!!, surveyId!!, reason, reward)
    }
}

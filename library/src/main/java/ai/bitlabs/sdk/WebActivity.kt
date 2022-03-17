package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.model.WebActivityParams
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

private const val TAG = "BL WebActivity"
const val BUNDLE_KEY_PARAMS = "bundle-key-params"

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
            Log.e(TAG, "No bundle data found!")
            finish()
            return
        }

        bindUI()

        if (Build.VERSION.SDK_INT >= 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        else
            CookieManager.getInstance().setAcceptCookie(true)


        if (savedInstanceState == null)
            webView?.loadUrl(params.getURL())
    }

    override fun onBackPressed() {
        if (toolbar?.visibility == View.VISIBLE)
            leaveSurveyAlert()
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
            leaveSurveyAlert()
        return super.onOptionsItemSelected(item)
    }

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
        setupWebView()
    }

    private fun setupWebView() {
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

    private fun toggleToolbar(isPageOfferWall: Boolean) {
        toolbar?.visibility = if (isPageOfferWall) View.GONE else View.VISIBLE
        closeButton?.visibility = if (isPageOfferWall) View.VISIBLE else View.GONE
        (if (isPageOfferWall) closeButton else toolbar)?.bringToFront()

        webView
            ?.apply { isScrollbarFadingEnabled = !isPageOfferWall }
            ?.settings?.run {
                setSupportZoom(!isPageOfferWall)
                builtInZoomControls = !isPageOfferWall
            }
    }

    private fun leaveSurveyAlert() {
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

    private fun leaveSurvey(reason: String) {
        toggleToolbar(true)
        webView?.loadUrl(params.getURL())

        if (networkId != null && surveyId != null)
            params.leaveSurveyListener.leaveSurvey(networkId!!, surveyId!!, reason, reward)
    }
}

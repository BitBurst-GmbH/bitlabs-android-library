package ai.bitlabs.sdk

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.Serializable

data class WebActivityParams(var token: String, var userID: String) : Serializable {
    var tags: Map<String, Any>? = null

    @Transient
    private var _url: String? = null;
    val url: String
        get() {
            if (_url == null) {
                val builder = Uri.parse("https://web.bitlabs.ai").buildUpon()
                builder.appendQueryParameter("token", token).appendQueryParameter("uid", userID)
                tags?.forEach { e -> builder.appendQueryParameter(e.key, e.value.toString()) }
                _url = builder.build().toString()
            }
            return _url ?: ""
        }
}

class WebActivity : AppCompatActivity() {
    companion object {
        const val BUNDLE_KEY_DATA = "data";
    }

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null
    private var closeButton: ImageView? = null

    private lateinit var params: WebActivityParams

    private var lastNetworkID: String? = null
    private var lastSurveyID: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        val paramsRaw = intent.extras?.getSerializable(BUNDLE_KEY_DATA) as? WebActivityParams
        if (paramsRaw == null) {
            Log.e("BitLabs", "no bundle data supplied to web activity")
            finish()
            return
        }
        params = paramsRaw

        Log.i("BitLabs", "Base URL: ${params.url}")

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        closeButton = findViewById(R.id.close)
        closeButton?.setOnClickListener { finish() }
        hideToolbar()

        webView = findViewById(R.id.web)
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView?.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val result = view.hitTestResult
                var data = result.extra
                if (data == null)
                    data = view.url
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this@WebActivity, Uri.parse(data))
                return false
            }
        }
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) {
                    return false
                }

                if (url.contains("web.bitlabs.ai")) {
                    hideToolbar()
                } else {
                    showToolbar()

                    val match = Regex("/networks/(\\d+)/surveys/(\\d+)").find(url)
                    if (match != null) {
                        val (network_id, survey_id) = match.destructured
                        lastNetworkID = network_id
                        lastSurveyID = survey_id
                    }
                }

                webView?.loadUrl(url)
                return true
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPageFinished(view: WebView?, url: String?) {
                CookieManager.getInstance().flush()
            }
        }

        val settings = webView?.settings
        settings?.javaScriptEnabled = true
        settings?.javaScriptCanOpenWindowsAutomatically = true
        settings?.setSupportMultipleWindows(true)
        settings?.domStorageEnabled = true
        settings?.allowFileAccess = true
        settings?.databaseEnabled = true
        settings?.cacheMode = WebSettings.LOAD_NO_CACHE

        if (Build.VERSION.SDK_INT >= 17) {
            settings?.mediaPlaybackRequiresUserGesture = false
        }
        if (Build.VERSION.SDK_INT >= 19) {
            webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        if (savedInstanceState == null) {
            webView?.loadUrl(params.url)
        }
    }

    private fun hideToolbar() {
        toolbar?.visibility = View.GONE
        closeButton?.visibility = View.VISIBLE
        closeButton?.bringToFront()

        val settings = webView?.settings
        settings?.setSupportZoom(false)
        settings?.builtInZoomControls = false
        settings?.displayZoomControls = true
        webView?.isScrollbarFadingEnabled = false
    }

    private fun showToolbar() {
        toolbar?.visibility = View.VISIBLE
        toolbar?.bringToFront()
        closeButton?.visibility = View.GONE

        val settings = webView?.settings
        settings?.setSupportZoom(true)
        settings?.builtInZoomControls = true
        settings?.displayZoomControls = false
        webView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView?.isScrollbarFadingEnabled = true
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
        hideToolbar()
        webView?.loadUrl(params.url)

        if (lastNetworkID != null && lastSurveyID != null)
            BitLabsSDK.instance.reportSurveyLeave(lastNetworkID ?: "", lastSurveyID ?: "", reason)
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
}

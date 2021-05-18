package ai.bitlabs.sdk

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import org.json.JSONObject


class WebActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null
    private var closeButton: ImageView? = null

    private var baseURL: String? = null
    private var bundle: Bundle? = null

    private var latestNetworkId: String? = null
    private var latestSurveyId: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        bundle = intent.extras!!
        if (bundle == null) {
            System.err.println("missing parameters")
            finish()
            return
        }

        baseURL = String.format(
                "https://web.bitlabs.ai/?uid=%s&token=%s%s",
                bundle!!.getString(BUNDLE_KEY_USER_ID),
                bundle!!.getString(BUNDLE_KEY_TOKEN),
                bundle!!.getString(BUNDLE_KEY_TAGS)
        )

        Log.i("BitLabs", "Base URL: " + baseURL);

        val dark = Color.parseColor(bundle!!.getString(BUNDLE_KEY_COLOR_LIGHT))
        toolbar = if ((dark.red * 0.299 + dark.green * 0.587 + dark.blue * 0.114) > 186)
            findViewById(R.id.toolbar)
        else
            findViewById(R.id.toolbarLight)

        toolbar!!.title = getString(R.string.web_toolbar_header)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = Color.parseColor(bundle!!.getString(BUNDLE_KEY_COLOR_DARK))
        toolbar!!.setBackgroundColor(dark)

        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        closeButton = findViewById(R.id.close)
        closeButton!!.setOnClickListener { finish() }
        closeButton!!.bringToFront()

        webView = findViewById(R.id.web)
        webView!!.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView!!.webChromeClient = object : WebChromeClient() {
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
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.contains("web.bitlabs.ai")) {
                        hideToolbar()
                    } else {
                        showToolbar()

                        val match = Regex("\\/networks\\/(\\d+)\\/surveys\\/(\\d+)").find(url)
                        if (match != null) {
                            val (network_id, survey_id) = match.destructured
                            latestNetworkId = network_id
                            latestSurveyId = survey_id
                        }
                    }

                    webView!!.loadUrl(url)
                    return true
                } else return false
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPageFinished(view: WebView?, url: String?) {
                CookieManager.getInstance().flush()
            }
        }

        val settings = webView!!.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(true)
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE;

        if (Build.VERSION.SDK_INT >= 17)
            settings.mediaPlaybackRequiresUserGesture = false
        if (Build.VERSION.SDK_INT >= 19)
            webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            webView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        else
            CookieManager.getInstance().setAcceptCookie(true)

        if (savedInstanceState == null)
            webView!!.loadUrl(baseURL!!)
    }


    private fun hideToolbar() {
        toolbar!!.visibility = View.GONE
        closeButton!!.visibility = View.VISIBLE
        closeButton!!.bringToFront()

        val settings = webView!!.settings
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = true
        webView!!.isScrollbarFadingEnabled = false
    }

    private fun showToolbar() {
        toolbar!!.visibility = View.VISIBLE
        closeButton!!.visibility = View.GONE
        toolbar!!.bringToFront()

        val settings = webView!!.settings
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        webView!!.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY;
        webView!!.isScrollbarFadingEnabled = true
    }

    private fun leaveSurveyAlert() {
        val options =
                arrayOf("SENSITIVE", "UNINTERESTING", "TECHNICAL", "TOO_LONG", "OTHER")
        val optionsDisplay =
                arrayOf(
                        getString(R.string.leave_reason_sensitive),
                        getString(R.string.leave_reason_uninteresting),
                        getString(R.string.leave_reason_technical),
                        getString(R.string.leave_reason_too_long),
                        getString(R.string.leave_reason_other)
                )

        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.leave_dialog_title))
                .setItems(optionsDisplay) { _, which ->
                    leaveSurvey(options[which])
                }
                .setNegativeButton(
                        getString(R.string.leave_dialog_continue)
                ) { _, _ -> }
                .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                Color.parseColor(
                        bundle?.getString(
                                BUNDLE_KEY_COLOR_ACCENT
                        )
                )
        )
    }

    private fun leaveSurvey(reason: String) {
        hideToolbar()
        webView!!.loadUrl(baseURL!!)

        if (latestNetworkId != null && latestSurveyId != null) {
            // Report Survey Leave
            val body = HashMap<String, String>()
            body["reason"] = reason
            val jsonObject = JSONObject(body as Map<*, *>)

            val requestQueue = Volley.newRequestQueue(this@WebActivity)
            val leaveRequest = object : JsonObjectRequest(
                    Method.POST,
                    String.format(
                            "https://api.bitlabs.ai/v1/client/networks/%s/surveys/%s/leave",
                            latestNetworkId,
                            latestSurveyId
                    ),
                    jsonObject,
                    Response.Listener {},
                    Response.ErrorListener {}
            ) {
                override fun getHeaders(): MutableMap<String?, String?>? {
                    return BitLabsSDK.HEADER
                }
            }

            requestQueue.add(leaveRequest)
        }
    }

    override fun onBackPressed() {
        if (toolbar!!.visibility == View.VISIBLE)
            leaveSurveyAlert()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView!!.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView!!.restoreState(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            leaveSurveyAlert()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val BUNDLE_KEY_TOKEN = "token"
        const val BUNDLE_KEY_USER_ID = "user_id"
        const val BUNDLE_KEY_COLOR_DARK = "color_dark"
        const val BUNDLE_KEY_COLOR_LIGHT = "color_light"
        const val BUNDLE_KEY_COLOR_ACCENT = "color_accent"
        const val BUNDLE_KEY_TAGS = "tags"
    }
}
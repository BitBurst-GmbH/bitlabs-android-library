package ai.bitlabs.sdk

import ai.bitlabs.sdk.util.TAG
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import kotlin.math.log

/** Adds all necessary configurations for the its receiver [WebActivity.webView] */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    context: Context,
    onShouldOverrideUrlLoading: (isPageOfferWall: Boolean, url: String) -> Unit
) {

    if (Build.VERSION.SDK_INT >= 21)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    else
        CookieManager.getInstance().setAcceptCookie(true)

    this.setLayerType(
        if (Build.VERSION.SDK_INT >= 19) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
        null
    )

    this.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            dialog: Boolean,
            userGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val data = view.hitTestResult.extra ?: view.url
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(data))
            return false
        }
    }


    this.webViewClient = object : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url == null) return false

            onShouldOverrideUrlLoading(url.contains("web.bitlabs.ai"), url)

            this@setup.loadUrl(url)
            return true
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.run { url.toString() } ?: return false

            onShouldOverrideUrlLoading(url.contains("web.bitlabs.ai"), url)

            this@setup.loadUrl(url)
            return true
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }
    }

    this.settings.run {
        displayZoomControls = false
        databaseEnabled = true
        allowFileAccess = true
        javaScriptEnabled = true
        domStorageEnabled = true
        setSupportMultipleWindows(true)
        cacheMode = WebSettings.LOAD_NO_CACHE
        javaScriptCanOpenWindowsAutomatically = true

        if (Build.VERSION.SDK_INT >= 17) mediaPlaybackRequiresUserGesture = false
    }
}
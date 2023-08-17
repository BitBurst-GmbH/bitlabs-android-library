package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.views.WebActivity
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent

/** Adds all necessary configurations for the its receiver [WebActivity.webView] */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    context: Context, onShouldOverrideUrlLoading: (isPageOfferWall: Boolean, url: String) -> Unit
) {
    if (Build.VERSION.SDK_INT >= 21) CookieManager.getInstance()
        .setAcceptThirdPartyCookies(this, true)
    else CookieManager.getInstance().setAcceptCookie(true)

    this.setLayerType(View.LAYER_TYPE_HARDWARE, null)

    this.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            dialog: Boolean,
            userGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val newWebView = WebView(view.context)
            with(resultMsg.obj as WebView.WebViewTransport) { webView = newWebView }
            resultMsg.sendToTarget()

            newWebView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url.isNullOrEmpty()) return true
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                    return false
                }

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.run { url.toString() } ?: return true
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                    return false
                }
            }
            return true
        }
    }

    this.webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            onShouldOverrideUrlLoading(
                url?.contains("https://web.bitlabs.ai") ?: true,
                url ?: ""
            )
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }
    }

    this.settings.run {
        databaseEnabled = true
        allowFileAccess = true
        javaScriptEnabled = true
        domStorageEnabled = true
        displayZoomControls = false
        setSupportMultipleWindows(true)
        cacheMode = WebSettings.LOAD_NO_CACHE
        mediaPlaybackRequiresUserGesture = false
        javaScriptCanOpenWindowsAutomatically = true
    }
}
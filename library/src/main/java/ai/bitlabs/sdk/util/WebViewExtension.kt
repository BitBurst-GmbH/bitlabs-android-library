package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.WebViewError
import ai.bitlabs.sdk.views.WebActivity
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

/** Adds all necessary configurations for the its receiver [WebActivity.webView] */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    context: Context,
    onDoUpdateVisitedHistory: (isPageOfferWall: Boolean, url: String) -> Unit,
    onError: (error: WebViewError?, date: String, url: String) -> Unit,
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
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    if (!url.isNullOrEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                    super.doUpdateVisitedHistory(view, url, isReload)
                }
            }

            return true
        }
    }

    this.webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            onDoUpdateVisitedHistory(
                url?.startsWith("https://web.bitlabs.ai") ?: true,
                url ?: ""
            )
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            if (BitLabs.debugMode || errorResponse?.statusCode == 404)
                onError(
                    WebViewError(errorResponse = errorResponse),
                    System.currentTimeMillis().toString(),
                    request?.url.toString()
                )
            super.onReceivedHttpError(view, request, errorResponse)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.d(TAG, "onReceivedError: ${error?.description}")
            if (BitLabs.debugMode
                || error?.description?.contains("ERR_CLEARTEXT_NOT_PERMITTED") == true
            )
                onError(
                    WebViewError(error = error),
                    System.currentTimeMillis().toString(),
                    request?.url.toString()
                )
            super.onReceivedError(view, request, error)
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
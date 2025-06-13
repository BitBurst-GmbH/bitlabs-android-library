package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import android.graphics.Bitmap
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

class BLWebViewClient(private val onError: (error: WebViewError) -> Unit) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.evaluateJavascript(
            """
            window.addEventListener('message', (event) => {
                window.AndroidWebView.postMessage(JSON.stringify(event.data));
            });
            """.trimIndent()
        ) {}
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        CookieManager.getInstance().flush()
    }

    override fun onReceivedHttpError(
        view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?,
    ) {
        if (BitLabs.debugMode) onError(
            WebViewError(url = request?.url.toString(), errorResponse = errorResponse)
        )

        super.onReceivedHttpError(view, request, errorResponse)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?, request: WebResourceRequest?, error: WebResourceError?,
    ) {
        if (BitLabs.debugMode || error?.description?.contains("ERR_CLEARTEXT_NOT_PERMITTED") == true)
            onError(WebViewError(url = request?.url.toString(), error = error))

        super.onReceivedError(view, request, error)
    }
}
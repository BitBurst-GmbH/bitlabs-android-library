package ai.bitlabs.sdk.util

import android.net.Uri
import android.os.Message
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

class BLWebChromeClient(private val onFileChosen: (ValueCallback<Array<Uri>>?) -> Unit) :
    WebChromeClient() {

    override fun onCreateWindow(
        view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message,
    ): Boolean {
        val newWebView = WebView(view.context)
        with(resultMsg.obj as WebView.WebViewTransport) { webView = newWebView }
        resultMsg.sendToTarget()

        newWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                newView: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val url = request?.url?.toString()
                if (!url.isNullOrEmpty()) {
                    CustomTabsIntent.Builder().build().launchUrl(view.context, url.toUri())
                    return true
                }
                return false
            }
        }

        return true
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        onFileChosen(filePathCallback)
        return true
    }
}
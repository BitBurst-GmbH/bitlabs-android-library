package ai.bitlabs.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    context: Context,
    onShouldOverrideUrlLoading: (isPageOfferWall: Boolean, url: String) -> Unit
) {
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
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url == null) return false

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
        javaScriptEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        domStorageEnabled = true
        allowFileAccess = true
        databaseEnabled = true
        cacheMode = WebSettings.LOAD_NO_CACHE

        if (Build.VERSION.SDK_INT >= 17) mediaPlaybackRequiresUserGesture = false
    }
}
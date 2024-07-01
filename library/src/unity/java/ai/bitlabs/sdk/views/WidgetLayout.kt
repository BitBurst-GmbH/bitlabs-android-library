package ai.bitlabs.sdk.views

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import android.widget.FrameLayout

class WidgetLayout(context: Context) : FrameLayout(context) {
    private val webView = WebView(context)

    init {
        webView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(webView)
        (context as Activity).addContentView(
            this,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun setSize(width: Int, height: Int) {
        layoutParams = LayoutParams(width, height)
    }

    fun setPosition(x: Int, y: Int) {
        val params = layoutParams as LayoutParams
        params.leftMargin = x
        params.topMargin = y
        layoutParams = params
    }
}
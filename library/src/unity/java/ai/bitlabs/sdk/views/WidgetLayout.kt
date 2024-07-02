package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.data.model.WidgetType
import ai.bitlabs.sdk.util.TAG
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout

class WidgetLayout(context: Context) : FrameLayout(context) {
    private val webView = WebView(context)

    private val widgetHtml = """
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="utf-8" />
                        <style>
                          html,
                          body,
                          #widget {
                            height: 100%;
                            margin: 0;
                          }
                        </style>
                    <script src="https://sdk.bitlabs.ai/bitlabs-sdk-v0.0.2.js"></script>
                    <link
                        rel="stylesheet"
                        href="https://sdk.bitlabs.ai/bitlabs-sdk-v0.0.2.css"
                    />
                    <title>BitLabs Widget</title>
                </head>
                <body>
                    <div id="widget"></div>

                    <script>
                          function initSDK() {
                            window.bitlabsSDK.init("{APP_TOKEN}", "{USER_ID}")
                              .then(() => {
                                window.bitlabsSDK.showWidget(
                                  "#widget",
                                  "{WIDGET_TYPE}",
                                  { onClick: undefined }
                                );
                                
                                document.removeEventListener("DOMContentLoaded", this.initSDK);
                              });
                          }

                        document.addEventListener("DOMContentLoaded", this.initSDK);
                    </script>
                </body>
            </html>
        """.trimIndent()

    init {
        webView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(webView)
        (context as Activity).addContentView(
            this,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun render(token: String, uid: String, widgetType: String) {
        val html = widgetHtml
            .replace("{APP_TOKEN}", token)
            .replace("{USER_ID}", uid)
            .replace("{WIDGET_TYPE}", widgetType)

        webView.settings.javaScriptEnabled = true

        webView.setBackgroundColor(Color.TRANSPARENT)

        webView.loadDataWithBaseURL(
            "https://sdk.bitlabs.ai",
            html,
            "text/html",
            "UTF-8",
            null
        )
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
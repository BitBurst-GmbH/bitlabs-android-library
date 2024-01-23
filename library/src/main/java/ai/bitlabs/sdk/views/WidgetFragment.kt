package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WidgetType
import ai.bitlabs.sdk.util.log
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment

class WidgetFragment(
    private val uid: String,
    private val token: String,
    private val widgetType: WidgetType,
) : Fragment(R.layout.fragment_widget) {

    private var webView: WebView? = null

    private val widgetHtml = """
            <!DOCTYPE html>
            <html style="height: 100%" lang="en">
                <head>
                    <meta charset="utf-8" />
                    <script src="https://sdk.bitlabs.ai/bitlabs-sdk-v0.0.2.js"></script>
                    <link
                        rel="stylesheet"
                        href="https://sdk.bitlabs.ai/bitlabs-sdk-v0.0.2.css"
                    />
                    <title>BitLabs Widget</title>
                </head>
                <body style="height: 96%">
                    <div id="widget" style="height: 100%"></div>

                    <script>
                          function initSDK() {
                            window.bitlabsSDK.init("${token}", "${uid}")
                              .then(() => {
                                window.bitlabsSDK.showWidget(
                                  "#widget",
                                  "${widgetType.asString}",
                                  { onClick: () => undefined }
                                );
                                
                                document.removeEventListener("DOMContentLoaded", this.initSDK);
                              });
                          }

                        document.addEventListener("DOMContentLoaded", this.initSDK);
                    </script>
                </body>
            </html>
        """.trimIndent()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView(view)

        webView?.loadDataWithBaseURL(
            "https://sdk.bitlabs.ai",
            widgetHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(view: View) {
        webView = view.findViewById(R.id.widget_webview)

        webView?.settings?.javaScriptEnabled = true

        webView?.setBackgroundColor(Color.TRANSPARENT)

        if (widgetType != WidgetType.LEADERBOARD) webView?.layoutParams?.height = 500

        webView?.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.log()
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }
}
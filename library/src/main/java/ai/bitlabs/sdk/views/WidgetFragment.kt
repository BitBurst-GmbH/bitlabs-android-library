package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WidgetType
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
                            // Replace YOUR_APP_TOKEN with your API token from the dashboard, insert USER_ID dynamically for each user
                            window.bitlabsSDK.init("${token}", "${uid}")
                              .then(() => {
                                // You can change the HTML element id or the offerwall position if you like
                                window.bitlabsSDK.showWidget(
                                  "#widget",
                                  "${widgetType.asString}",
                                  "bottom-left",
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView(view)

        webView?.loadDataWithBaseURL(
            "https://www.google.com",
            widgetHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(view: View) {
        webView = view.findViewById(R.id.widget_webview)


        webView?.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    Log.e(
                        "BitLabs",
                        "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                    )
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }

        webView?.settings?.javaScriptEnabled = true
    }
}
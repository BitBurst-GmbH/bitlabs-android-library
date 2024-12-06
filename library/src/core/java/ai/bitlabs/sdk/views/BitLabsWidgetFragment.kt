package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.bitlabs.WidgetType
import ai.bitlabs.sdk.util.log
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment

class BitLabsWidgetFragment(
    uid: String,
    token: String,
    private val widgetType: WidgetType,
) : Fragment(R.layout.fragment_widget_bitlabs) {

    private var webView: WebView? = null

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
                            window.bitlabsSDK.init("$token", "$uid")
                              .then(() => {
                                window.bitlabsSDK.showWidget(
                                  "#widget",
                                  "${widgetType.asString}",
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
            "https://sdk.bitlabs.ai",
            widgetHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupWebView(view: View) {
        webView = view.findViewById(R.id.widget_webview_bitlabs)

        webView?.settings?.javaScriptEnabled = true

        webView?.setBackgroundColor(Color.TRANSPARENT)

        webView?.layoutParams?.apply {
            if (widgetType == WidgetType.SIMPLE) {
                height = 359
                width = 800
            }

            if (widgetType == WidgetType.COMPACT) {
                height = 205
                width = 710
            }

            if (widgetType == WidgetType.FULL_WIDTH) {
                height = 160
            }
        }

        webView?.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.log()
                return super.onConsoleMessage(consoleMessage)
            }
        }

        webView?.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_UP -> {
                    // check if the action was a click not any other action
                    if (e.eventTime - e.downTime < 200) {
                        v.performClick()
                        BitLabs.launchOfferWall(requireActivity())
                    }
                }
            }
            false
        }
    }
}
package ai.bitlabs.sdk.util.extensions

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.util.HookName
import ai.bitlabs.sdk.util.RewardArgs
import ai.bitlabs.sdk.util.SurveyStartArgs
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.asHookMessage
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/** Adds all necessary configurations for its receiver [BitLabsOfferwallActivity.webView] */
fun WebView.setupChromeClient(onFileChosen: (ValueCallback<Array<Uri>>?) -> Unit) {
    this.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message,
        ): Boolean {
            val newWebView = WebView(view.context)
            with(resultMsg.obj as WebView.WebViewTransport) { webView = newWebView }
            resultMsg.sendToTarget()

            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?,
                ): Boolean {
                    val url = request?.url?.toString()
                    if (!url.isNullOrEmpty()) {
                        CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
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
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupSettings() = this.settings.run {
    databaseEnabled = true
    allowFileAccess = true
    javaScriptEnabled = true
    domStorageEnabled = true
    displayZoomControls = false
    cacheMode = WebSettings.LOAD_NO_CACHE
    setSupportMultipleWindows(true)
    mediaPlaybackRequiresUserGesture = false
    javaScriptCanOpenWindowsAutomatically = true
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
}

fun WebView.setupClient(onError: (error: WebViewError) -> Unit) {
    // You're probably wondering why we set the layout params.
    // The weird reason is that this is the only way to ensure that the Webview
    // renders VueJS components correctly when the WebView is used in a Composable.
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    CookieManager.getInstance()
        .setAcceptThirdPartyCookies(this, true)

    this.webViewClient = object : WebViewClient() {
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
}

fun WebView.setupPostMessageHandler(
    addReward: (reward: Double) -> Unit,
    setClickId: (clickId: String?) -> Unit,
    toggleTopBar: (Boolean) -> Unit,
) = addJavascriptInterface(object {
    @JavascriptInterface
    fun postMessage(message: String) {
        val hookMessage = message.asHookMessage() ?: return

        if (hookMessage.type != "hook") return

        when (hookMessage.name) {
            HookName.SDK_CLOSE -> {
                (context as BitLabsOfferwallActivity).finish()
            }

            HookName.SURVEY_START -> {
                val surveyStartArgs = hookMessage.args
                    .filterIsInstance<SurveyStartArgs>()
                    .firstOrNull()
                val clickId = surveyStartArgs?.clickId
                setClickId(clickId)
                (context as BitLabsOfferwallActivity).runOnUiThread { toggleTopBar(true) }
                Log.i(TAG, "Caught Survey Start event with clickId: $clickId")
            }

            HookName.SURVEY_COMPLETE -> {
                val rewardArgs = hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()
                val reward = rewardArgs?.reward ?: 0f
                addReward(reward.toDouble())
                Log.i(TAG, "Caught Survey Complete event with reward: $reward")
            }

            HookName.SURVEY_SCREENOUT -> {
                val rewardArgs = hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()
                val reward = rewardArgs?.reward ?: 0f
                addReward(reward.toDouble())
                Log.i(TAG, "Caught Survey Screenout with reward: $reward")
            }

            HookName.SURVEY_START_BONUS -> {
                val rewardArgs = hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()
                val reward = rewardArgs?.reward ?: 0f
                addReward(reward.toDouble())
                Log.i(TAG, "Caught Survey Start Bonus event with reward: $reward")
            }

            HookName.INIT -> {
                (context as BitLabsOfferwallActivity).runOnUiThread { toggleTopBar(false) }
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        this@setupPostMessageHandler.evaluateJavascript(
                            """
                                window.parent.postMessage({ target: 'app.behaviour.close_button_visible', value: true });
                                """.trimIndent()
                        ) {}
                    }, 1000
                )
            }
        }
    }
}, "AndroidWebView")

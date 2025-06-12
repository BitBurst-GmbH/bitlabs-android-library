package ai.bitlabs.sdk.offerwall.util

import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.util.TAG
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup() = apply {
    // You're probably wondering why we set the layout params.
    // The weird reason is that this is the only way to ensure that the Webview
    // renders VueJS components correctly when the WebView is used in a Composable.
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    CookieManager.getInstance()
        .setAcceptThirdPartyCookies(this, true)
}.settings.run {
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

fun WebView.setupPostMessageHandler(
    addReward: (reward: Double) -> Unit,
    setClickId: (clickId: String?) -> Unit,
    toggleTopBar: (Boolean) -> Unit,
    token: String, uid: String,
) = addJavascriptInterface(object {
    @JavascriptInterface
    fun postMessage(message: String) {
        val hookMessage = message.asHookMessage(token, uid) ?: return

        if (hookMessage.type != "hook") return

        when (hookMessage.name) {
            HookName.SDK_CLOSE -> {
                (context as Activity).finish()
            }

            HookName.SURVEY_START -> {
                val surveyStartArgs = hookMessage.args
                    .filterIsInstance<SurveyStartArgs>()
                    .firstOrNull()
                val clickId = surveyStartArgs?.clickId
                setClickId(clickId)
                (context as Activity).runOnUiThread { toggleTopBar(true) }
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

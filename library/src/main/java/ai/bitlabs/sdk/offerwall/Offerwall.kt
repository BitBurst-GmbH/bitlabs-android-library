package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.offerwall.util.OfferwallListenerManager
import ai.bitlabs.sdk.offerwall.util.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_LISTENER_ID
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import android.content.Context
import android.content.Intent

data class Offerwall(
    private val token: String,
    private val uid: String,
    val options: MutableMap<String, Any> = mutableMapOf(),
    val tags: MutableMap<String, Any> = mutableMapOf(),
    var onSurveyRewardListener: OnSurveyRewardListener = OnSurveyRewardListener { },
    var onOfferwallClosedListener: OnOfferwallClosedListener = OnOfferwallClosedListener { },
) {

    private val listenerId = hashCode()

    @JvmOverloads
    fun launch(context: Context, sdk: String = "NATIVE") {
        val url = WebActivityParams(token, uid, sdk, "", tags).url
        val intent = Intent(context, BitLabsOfferwallActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            putExtra(BUNDLE_KEY_TOKEN, token)
            putExtra(BUNDLE_KEY_LISTENER_ID, listenerId)
        }

        OfferwallListenerManager.registerListeners(
            listenerId,
            onSurveyRewardListener,
            onOfferwallClosedListener
        )

        context.startActivity(intent)
    }
}
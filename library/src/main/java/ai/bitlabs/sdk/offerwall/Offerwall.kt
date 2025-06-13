package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.offerwall.util.OfferwallListenerManager
import ai.bitlabs.sdk.offerwall.util.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_LISTENER_ID
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_UID
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val TAG = "BitLabs.Offerwall"

data class Offerwall(
    private val token: String,
    private val uid: String,
    val options: MutableMap<String, Any> = mutableMapOf(),
    val tags: MutableMap<String, Any> = mutableMapOf(),
    var onSurveyRewardListener: OnSurveyRewardListener = OnSurveyRewardListener { },
    var onOfferwallClosedListener: OnOfferwallClosedListener = OnOfferwallClosedListener { },
) {
    private val listenerId = hashCode()
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    private fun determineAdvertisingInfo(context: Context) = try {
        val id = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
        Log.d(TAG, "Advertising Id: $id")
        id
    } catch (e: Exception) {
        SentryManager.captureException(token, uid, e)
        Log.e(TAG, "Failed to determine Advertising Id", e)
        ""
    }

    @JvmOverloads
    fun launch(context: Context, sdk: String = "NATIVE") = coroutineScope.launch {
        val adId = determineAdvertisingInfo(context)

        val url = WebActivityParams(token, uid, sdk, adId, tags).url
        val intent = Intent(context, BitLabsOfferwallActivity::class.java).apply {
            putExtra(BUNDLE_KEY_URL, url)
            putExtra(BUNDLE_KEY_UID, uid)
            putExtra(BUNDLE_KEY_TOKEN, token)
            putExtra(BUNDLE_KEY_LISTENER_ID, listenerId)
        }

        OfferwallListenerManager.registerListeners(
            listenerId,
            onSurveyRewardListener,
            onOfferwallClosedListener
        )

        withContext(Dispatchers.Main) { context.startActivity(intent) }
    }
}
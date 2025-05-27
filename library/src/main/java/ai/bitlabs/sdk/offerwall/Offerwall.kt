package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.util.BUNDLE_KEY_LISTENER_ID
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extractColors
import ai.bitlabs.sdk.util.getColorScheme
import android.content.Context
import android.content.Intent
import android.util.Log

data class Offerwall(
    private val token: String,
    private val uid: String,
    val options: MutableMap<String, Any> = mutableMapOf(),
    val tags: MutableMap<String, Any> = mutableMapOf(),
    var onSurveyRewardListener: OnSurveyRewardListener = OnSurveyRewardListener { },
    var onOfferwallClosedListener: OnOfferwallClosedListener = OnOfferwallClosedListener { },
) {

    private val listenerId = hashCode()
    private var headerColor = intArrayOf(0, 0)
    private var backgroundColors = intArrayOf(0, 0)

    init {
        BitLabs.bitLabsRepo?.getAppSettings(token, getColorScheme(), { app ->
            app.visual.run {
                headerColor =
                    extractColors(navigationColor).takeIf { it.isNotEmpty() } ?: headerColor
                backgroundColors =
                    extractColors(backgroundColor).takeIf { it.isNotEmpty() }
                        ?: backgroundColors
            }
        }, { Log.e(TAG, "$it") })
    }

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
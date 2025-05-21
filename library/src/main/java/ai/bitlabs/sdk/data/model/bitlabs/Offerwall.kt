package ai.bitlabs.sdk.data.model.bitlabs

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.util.BUNDLE_KEY_BACKGROUND_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_HEADER_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extractColors
import ai.bitlabs.sdk.util.getColorScheme
import ai.bitlabs.sdk.views.BitLabsOfferwallActivity
import android.content.Context
import android.content.Intent
import android.util.Log

data class Offerwall(
    private val token: String,
    private val uid: String,
    val tags: MutableMap<String, Any> = mutableMapOf(),
    val options: MutableMap<String, Any> = mutableMapOf(),
    var onSurveyReward: (Double) -> Unit = { },
    var onOfferwallClosed: (Double) -> Unit = { },
) {

    private var headerColor = intArrayOf(0, 0)
    private var backgroundColors = intArrayOf(0, 0)

    init {
        BitLabs.bitLabsRepo?.getAppSettings(getColorScheme(), { app ->
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_URL, url)
            putExtra(BUNDLE_KEY_HEADER_COLOR, headerColor)
            putExtra(BUNDLE_KEY_BACKGROUND_COLOR, backgroundColors)
        }

        context.startActivity(intent)
    }
}

package ai.bitlabs.sdk.offerwall.components.webview

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.offerwall.util.OfferwallListenerManager
import ai.bitlabs.sdk.offerwall.util.extractColors
import ai.bitlabs.sdk.offerwall.util.getColorScheme
import ai.bitlabs.sdk.offerwall.util.getLuminance
import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class BLWebViewViewModel(val token: String, val listenerId: Int) : ViewModel() {
    var clickId = ""
    val isColorBright: Boolean
        get() = getLuminance(headerColors.value.first()) > 0.729 * 255
                || getLuminance(headerColors.value.last()) > 0.729 * 255

    private var totalSurveyReward = 0.0

    private val onSurveyRewardListener: OnSurveyRewardListener? by lazy {
        OfferwallListenerManager.getOnSurveyRewardListener(listenerId)
    }
    private val onOfferwallClosedListener: OnOfferwallClosedListener? by lazy {
        OfferwallListenerManager.getOnOfferwallClosedListener(listenerId)
    }

    private val _headerColors = mutableStateOf(intArrayOf(0, 0))
    val headerColors: State<IntArray> get() = _headerColors

    private val _backgroundColors = mutableStateOf(intArrayOf(0, 0))
    val backgroundColors: State<IntArray> get() = _backgroundColors

    init {
        BitLabs.bitLabsRepo?.getAppSettings(token, {
            val config = it.configuration
            val theme = getColorScheme()

            val navigationColor =
                config.find { it.internalIdentifier == "app.visual.$theme.navigation_color" }?.value
                    ?: ""
            _headerColors.value =
                extractColors(navigationColor).takeIf { it.isNotEmpty() } ?: _headerColors.value

            val backgroundColor =
                config.find { it.internalIdentifier == "app.visual.$theme.background_color" }?.value
                    ?: ""
            _backgroundColors.value =
                extractColors(backgroundColor).takeIf { it.isNotEmpty() }
                    ?: _backgroundColors.value
        }, { Log.e(TAG, "$it") })
    }

    fun leaveSurvey(reason: String) {
        if (clickId.isEmpty()) return
        // TODO: Handle leaveSurvey properly
        BitLabs.leaveSurvey(clickId, reason)
        clickId = ""
    }

    fun onSurveyReward(reward: Double) = onSurveyRewardListener?.onSurveyReward(reward).also {
        totalSurveyReward += reward
    }

    fun onOfferwallClosed() {
        onOfferwallClosedListener?.onOfferwallClosed(totalSurveyReward)
        BitLabs.onRewardListener?.onSurveyReward(totalSurveyReward)
    }
}
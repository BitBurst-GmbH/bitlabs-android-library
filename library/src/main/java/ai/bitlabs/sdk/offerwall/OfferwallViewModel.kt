package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extractColors
import ai.bitlabs.sdk.util.getColorScheme
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class OfferwallViewModel(val token: String, val listenerId: Int) : ViewModel() {
    var clickId = ""
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
        BitLabs.bitLabsRepo?.getAppSettings(token, getColorScheme(), { app ->
            app.visual.run {
                _headerColors.value = extractColors(navigationColor)
                    .takeIf { it.isNotEmpty() }
                    ?: _headerColors.value

                _backgroundColors.value = extractColors(backgroundColor)
                    .takeIf { it.isNotEmpty() }
                    ?: backgroundColors.value
            }
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
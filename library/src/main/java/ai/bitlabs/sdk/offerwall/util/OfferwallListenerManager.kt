package ai.bitlabs.sdk.offerwall.util

import ai.bitlabs.sdk.util.OnOfferwallClosedListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener

internal object OfferwallListenerManager {
    private val onSurveyRewardListeners = mutableMapOf<Int, OnSurveyRewardListener>()
    private val onOfferwallClosedListeners = mutableMapOf<Int, OnOfferwallClosedListener>()

    fun registerListeners(
        id: Int,
        onSurveyRewardListener: OnSurveyRewardListener,
        onOfferwallClosedListener: OnOfferwallClosedListener,
    ) {
        onSurveyRewardListeners[id] = onSurveyRewardListener
        onOfferwallClosedListeners[id] = onOfferwallClosedListener
    }

    fun unregisterListeners(id: Int) {
        onSurveyRewardListeners.remove(id)
        onOfferwallClosedListeners.remove(id)
    }

    fun getOnSurveyRewardListener(id: Int) = onSurveyRewardListeners[id]

    fun getOnOfferwallClosedListener(id: Int) = onOfferwallClosedListeners[id]
}
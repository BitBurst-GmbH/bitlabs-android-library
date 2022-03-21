package ai.bitlabs.sdk.util

import java.io.Serializable

fun interface OnRewardListener : Serializable {
    fun onReward(payout: Float)
}

fun interface OnResponseListener {
    fun onResponse(response: Boolean?)
}

internal fun interface LeaveSurveyListener : Serializable {
    fun leaveSurvey(networkId: String, surveyId: String, reason: String, payout: Float)
}
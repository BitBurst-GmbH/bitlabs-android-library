package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.network.BitLabsAPI
import java.io.Serializable

/** Interface definition for a callback to be invoked when a reward is earned by the user. */
fun interface OnRewardListener : Serializable {
    fun onReward(payout: Float)
}


/** Interface definition for a callback to be invoked when a response is received from [BitLabsAPI]. */
fun interface OnResponseListener<T> {
    fun onResponse(response: T?)
}


/** Interface definition for a callback to be invoked when a user leaves the survey. */
internal fun interface LeaveSurveyListener : Serializable {
    fun leaveSurvey(networkId: String, surveyId: String, reason: String, payout: Float)
}
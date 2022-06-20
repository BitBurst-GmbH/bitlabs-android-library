package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.network.BitLabsAPI

/** Interface definition for a callback to be invoked when a reward is earned by the user. */
fun interface OnRewardListener {
    fun onReward(payout: Float)
}

/** Interface definition for a callback to be invoked when a response is received from [BitLabsAPI]. */
fun interface OnResponseListener<T> {
    fun onResponse(response: T)
}

/** Interface definition for a callback to be invoked when an exception is received from [BitLabsAPI]. */
fun interface OnExceptionListener {
    fun onException(exception: Exception)
}
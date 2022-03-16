package ai.bitlabs.sdk.util

fun interface OnRewardListener {
    fun onReward(payout: Float)
}

fun interface OnResponseListener {
    fun onResponse(response: Boolean?)
}
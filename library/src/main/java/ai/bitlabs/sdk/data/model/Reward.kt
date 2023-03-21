package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class Reward(
    val rank: Int,
    @SerializedName("reward_raw") val rewardRaw: Double
)
package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Reward(
    val rank: Int,
    @SerializedName("reward_raw") val rewardRaw: Double
)
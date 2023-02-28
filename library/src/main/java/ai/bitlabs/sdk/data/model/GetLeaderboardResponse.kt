package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class GetLeaderboardResponse(
    @SerializedName("next_reset_at") val nextResetAt: String,
    @SerializedName("own_user") val ownUser: OwnUser,
    val rewards: List<Reward>,
    @SerializedName("top_users") val topUsers: List<TopUser>
)
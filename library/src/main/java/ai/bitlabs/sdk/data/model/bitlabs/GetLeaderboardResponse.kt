package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetLeaderboardResponse(
    @SerializedName("next_reset_at") val nextResetAt: String,
    @SerializedName("own_user") val ownUser: User?,
    val rewards: List<Reward>,
    @SerializedName("top_users") val topUsers: List<User>?
)
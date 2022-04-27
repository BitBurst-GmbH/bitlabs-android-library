package ai.bitlabs.sdk.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeaveReason(
    val reason: String
)

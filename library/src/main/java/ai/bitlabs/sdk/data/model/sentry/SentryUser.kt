package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.annotations.SerializedName

data class SentryUser(
    val id: String,
    val email: String? = null,
    val username: String? = null,
    @SerializedName("ip_address") val ipAddress: String = "{{auto}}"
)

package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("earnings_raw") val earningsRaw: Double,
    val name: String,
    val rank: Int
)
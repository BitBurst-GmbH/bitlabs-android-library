package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class User(
    @SerializedName("earnings_raw") val earningsRaw: Double,
    val name: String,
    val rank: Int
)
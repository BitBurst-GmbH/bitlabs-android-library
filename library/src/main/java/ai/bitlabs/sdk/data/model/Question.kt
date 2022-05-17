package ai.bitlabs.sdk.data.model


import com.google.gson.annotations.SerializedName

internal data class Question(
    @SerializedName("network_id") val networkId: Int,
    val id: String,
    val country: String,
    val language: String,
    val type: String,
    @SerializedName("localized_text") val localizedText: String,
    val answers: List<Answer>,
    @SerializedName("can_skip") val canSkip: Boolean
)
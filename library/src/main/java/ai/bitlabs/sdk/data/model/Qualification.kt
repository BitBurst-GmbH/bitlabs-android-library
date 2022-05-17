package ai.bitlabs.sdk.data.model


import com.google.gson.annotations.SerializedName

internal data class Qualification(
    @SerializedName("network_id") val networkId: Int,
    @SerializedName("question_id") val questionId: String,
    val country: String,
    val language: String,
    val question: Question,
    @SerializedName("is_standard_profile") val isStandardProfile: Boolean,
    @SerializedName("is_start_bonus") val isStartBonus: Boolean,
    val score: Double,
    val sequence: Int
)
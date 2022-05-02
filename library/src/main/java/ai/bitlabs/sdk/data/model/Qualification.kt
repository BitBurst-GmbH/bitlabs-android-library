package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Qualification(
    @Json(name = "network_id") val networkId: Int,
    @Json(name = "question_id") val questionId: String,
    val country: String,
    val language: String,
    val question: Question,
    @Json(name = "is_standard_profile") val isStandardProfile: Boolean,
    @Json(name = "is_start_bonus") val isStartBonus: Boolean,
    val score: Double,
    val sequence: Int
)
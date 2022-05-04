package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

internal data class Qualification(
    @field:Json(name = "network_id") val networkId: Int,
    @field:Json(name = "question_id") val questionId: String,
    val country: String,
    val language: String,
    val question: Question,
    @field:Json(name = "is_standard_profile") val isStandardProfile: Boolean,
    @field:Json(name = "is_start_bonus") val isStartBonus: Boolean,
    val score: Double,
    val sequence: Int
)
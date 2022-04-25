package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

data class Survey(
    @Json(name = "network_id") val networkId: Int,
    val id: Int,
    val cpi: String,
    val value: String,
    val loi: Double,
    val remaining: Int,
    val details: Details,
    val rating: Int,
    val link: String,
    val score: Double,
    @Json(name = "missing_questions") val missingQuestions: Int
) {
    fun open() {
        // open link
    }
}
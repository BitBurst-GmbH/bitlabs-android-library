package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Question(
    @Json(name = "network_id") val networkId: Int,
    val id: String,
    val country: String,
    val language: String,
    val type: String,
    @Json(name = "localized_text") val localizedText: String,
    val answers: List<Answer>,
    @Json(name = "can_skip") val canSkip: Boolean
)
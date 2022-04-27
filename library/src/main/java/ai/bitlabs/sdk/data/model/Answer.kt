package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Answer(
    val code: String,
    @Json(name = "localized_text") val localizedText: String
)
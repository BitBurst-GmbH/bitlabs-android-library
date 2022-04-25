package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

data class Answer(
    val code: String,
    @Json(name = "localized_text") val localizedText: String
)
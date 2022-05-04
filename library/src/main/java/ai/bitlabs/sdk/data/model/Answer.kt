package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

internal data class Answer(
    val code: String,
    @field:Json(name = "localized_text") val localizedText: String
)
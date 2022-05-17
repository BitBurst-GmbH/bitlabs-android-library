package ai.bitlabs.sdk.data.model


import com.google.gson.annotations.SerializedName

internal data class Answer(
    val code: String,
    @SerializedName("localized_text") val localizedText: String
)
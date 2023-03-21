package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class Symbol(
    val content: String,
    @SerializedName("is_image") val isImage: Boolean
)
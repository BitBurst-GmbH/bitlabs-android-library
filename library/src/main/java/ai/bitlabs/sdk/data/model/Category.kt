package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    val name: String,
    @Json(name = "icon_url") val iconUrl: String
)
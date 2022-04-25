package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

data class Category(
    val name: String,
    @Json(name = "icon_url") val iconUrl: String
)
package ai.bitlabs.sdk.data.model


import com.google.gson.annotations.SerializedName

data class Category(
    val name: String,
    @SerializedName("icon_name") val iconName: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("name_internal") val nameInternal: String
)
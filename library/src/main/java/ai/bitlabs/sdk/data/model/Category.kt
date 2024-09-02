package ai.bitlabs.sdk.data.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Category(
    val name: String,
    @SerializedName("icon_name") val iconName: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("name_internal") val nameInternal: String
)
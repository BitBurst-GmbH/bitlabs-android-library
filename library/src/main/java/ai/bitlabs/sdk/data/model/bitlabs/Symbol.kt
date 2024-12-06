package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Symbol(
    val content: String,
    @SerializedName("is_image") val isImage: Boolean
)
package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** This data class is most likely used in all API responses in BitLabs API */
@Keep
internal data class BitLabsResponse<T>(
    val data: T?,
    val error: Error?,
    val status: String,
    @SerializedName("trace_id") val traceId: String
)
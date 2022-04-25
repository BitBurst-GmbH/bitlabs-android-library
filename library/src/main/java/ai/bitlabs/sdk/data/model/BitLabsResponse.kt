package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Json

/** This data class is most likely used in all API repsonses in BitLabs API */
internal data class BitLabsResponse<T>(
    val data: T?,
    val error: Error?,
    val status: String,
    @Json(name = "trace_id") val traceId: String
)
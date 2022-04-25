package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

data class GetActionsResponse(
    val `data`: Data,
    val error: Error,
    val status: String,
    @Json(name = "trace_id") val traceId: String
)
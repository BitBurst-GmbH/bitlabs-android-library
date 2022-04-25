package ai.bitlabs.sdk.data.model

/** This data class is most likely used in all API repsonses in BitLabs API */
internal data class BitLabsResponse(
    val data: CheckSurveysResponse?,
    val error: Error?,
    val status: String,
    val trace_id: String
)

internal data class CheckSurveysResponse(val has_surveys: Boolean)
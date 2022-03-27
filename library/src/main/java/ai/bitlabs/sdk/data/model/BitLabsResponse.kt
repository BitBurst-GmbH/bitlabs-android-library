package ai.bitlabs.sdk.data.model

/** This data class is most likely used in all API repsonses in BitLabs API */
internal data class BitLabsResponse(
    val data: CheckSurveysResponse?,
    val error: BitLabsError?,
    val status: String,
    val trace_id: String
)

internal data class CheckSurveysResponse(val has_surveys: Boolean)

internal data class BitLabsError(val details: ErrorDetails)

internal data class ErrorDetails(
    val http: String,
    val msg: String
)

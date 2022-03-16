package ai.bitlabs.sdk.data.model

data class BitLabsResponse(
    val data: CheckSurveysResponse,
    val error: BitLabsError,
    val status: String,
    val trace_id: String
)

data class CheckSurveysResponse(val has_surveys: Boolean)

data class BitLabsError(val details: ErrorDetails)

data class ErrorDetails(
    val http: String,
    val msg: String
)

package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Json

internal data class CheckSurveysResponse(
    @Json(name = "has_surveys") val hasSurveys: Boolean
)
package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CheckSurveysResponse(
    @Json(name = "has_surveys") val hasSurveys: Boolean
)
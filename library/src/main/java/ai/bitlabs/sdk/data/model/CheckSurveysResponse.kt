package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

internal data class CheckSurveysResponse(
    @SerializedName("has_surveys") val hasSurveys: Boolean
)
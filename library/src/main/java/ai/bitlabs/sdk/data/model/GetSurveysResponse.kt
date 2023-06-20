package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

internal data class GetSurveysResponse(
    @SerializedName("restriction_reason") val restrictionReason: RestrictionReason?,
    val surveys: List<Survey>,
)
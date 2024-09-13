package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class GetSurveysResponse(
    @SerializedName("restriction_reason") val restrictionReason: RestrictionReason?,
    val surveys: List<Survey>,
)
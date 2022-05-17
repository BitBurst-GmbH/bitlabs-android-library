package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

internal data class GetActionsResponse(
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("start_bonus") val startBonus: StartBonus?,
    @SerializedName("restriction_reason") val restrictionReason: RestrictionReason?,
    val surveys: List<Survey>,
    val qualification: Qualification?
)
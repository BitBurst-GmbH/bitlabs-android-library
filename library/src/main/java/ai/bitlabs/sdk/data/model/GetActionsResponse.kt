package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Json


internal data class GetActionsResponse(
    @field:Json(name = "is_new_user") val isNewUser: Boolean,
    @field:Json(name = "start_bonus") val startBonus: StartBonus?,
    @field:Json(name = "restriction_reason") val restrictionReason: RestrictionReason?,
    val surveys: List<Survey>,
    val qualification: Qualification?
)
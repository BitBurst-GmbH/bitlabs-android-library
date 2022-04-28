package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GetActionsResponse(
    @Json(name = "is_new_user") val isNewUser: Boolean,
    @Json(name = "start_bonus") val startBonus: StartBonus?,
    @Json(name = "restriction_reason") val restrictionReason: RestrictionReason?,
    val surveys: List<Survey>,
    val qualification: Qualification?
)
package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RestrictionReason(
    @Json(name = "not_verified") val notVerified: Boolean,
    @Json(name = "using_vpn") val usingVpn: Boolean,
    @Json(name = "banned_until") val bannedUntil: String,
    val reason: String,
    @Json(name = "unsupported_country") val unsupportedCountry: String
)
package ai.bitlabs.sdk.data.model


import com.squareup.moshi.Json

internal data class RestrictionReason(
    @field:Json(name = "not_verified") val notVerified: Boolean?,
    @field:Json(name = "using_vpn") val usingVpn: Boolean?,
    @field:Json(name = "banned_until") val bannedUntil: String?,
    val reason: String?,
    @field:Json(name = "unsupported_country") val unsupportedCountry: String?
)
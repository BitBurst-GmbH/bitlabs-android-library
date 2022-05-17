package ai.bitlabs.sdk.data.model


import com.google.gson.annotations.SerializedName

internal data class RestrictionReason(
    @SerializedName("not_verified") val notVerified: Boolean?,
    @SerializedName("using_vpn") val usingVpn: Boolean?,
    @SerializedName("banned_until") val bannedUntil: String?,
    val reason: String?,
    @SerializedName("unsupported_country") val unsupportedCountry: String?
)
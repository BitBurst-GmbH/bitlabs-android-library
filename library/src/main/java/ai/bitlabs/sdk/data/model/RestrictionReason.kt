package ai.bitlabs.sdk.data.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class RestrictionReason(
    @SerializedName("not_verified") val notVerified: Boolean?,
    @SerializedName("using_vpn") val usingVpn: Boolean?,
    @SerializedName("banned_until") val bannedUntil: String?,
    val reason: String?,
    @SerializedName("unsupported_country") val unsupportedCountry: String?
) {
    fun prettyPrint(): String {
        if (notVerified == true) return "The publisher account that owns this app has not been verified and therefore cannot receive surveys."
        if (usingVpn == true) return "The user is using a VPN and cannot access surveys."
        if (bannedUntil != null) return "The user is banned until $bannedUntil"
        if (unsupportedCountry != null) return "Unsupported Country: $unsupportedCountry"
        return reason ?: "Unknown Reason"
    }
}
package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep

@Keep
data class GetAppSettingsResponse(
    val visual: Visual,
    val currency: Currency,
    val promotion: Promotion?
)
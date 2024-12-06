package ai.bitlabs.sdk.data.model.bitlabs

import androidx.annotation.Keep


@Keep
internal data class Error(
    val details: ErrorDetails
)
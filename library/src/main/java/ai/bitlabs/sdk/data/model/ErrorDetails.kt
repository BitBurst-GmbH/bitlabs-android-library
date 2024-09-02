package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep


@Keep
internal data class ErrorDetails(
    val http: String,
    val msg: String
)
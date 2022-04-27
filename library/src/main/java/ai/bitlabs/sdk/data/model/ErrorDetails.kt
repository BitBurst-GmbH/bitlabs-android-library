package ai.bitlabs.sdk.data.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
internal data class ErrorDetails(
    val http: String,
    val msg: String
)
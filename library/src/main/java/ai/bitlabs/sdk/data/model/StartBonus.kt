package ai.bitlabs.sdk.data.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
internal data class StartBonus(
    val reward: String
)
package ai.bitlabs.sdk.data.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Details(
    val category: Category
)
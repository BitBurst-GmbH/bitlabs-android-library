package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Moshi
import okhttp3.ResponseBody

/**
 * Returns a [BitLabsResponse] object converted from JSON.
 * @receiver The [error body][ResponseBody] of a response that received a status code in 400..500
 */
internal fun ResponseBody.body(): BitLabsResponse? =
    try {
        Moshi.Builder().build().adapter(BitLabsResponse::class.java).fromJson(this.string())
    } catch (e: Exception) {
        null
    }
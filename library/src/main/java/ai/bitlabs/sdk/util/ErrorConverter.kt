package ai.bitlabs.sdk.data.model

import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import java.lang.reflect.TypeVariable

/**
 * Returns a [BitLabsResponse] object converted from JSON.
 * @receiver The [error body][ResponseBody] of a response that received a status code in 400..500
 */
internal inline fun <reified T> ResponseBody.body(): BitLabsResponse<T>? =
    try {
        Moshi.Builder().build().adapter<BitLabsResponse<T>>(BitLabsResponse::class.java).fromJson(this.string())
    } catch (e: Exception) {
        null
    }
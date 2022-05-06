package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.model.BitLabsResponse
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.ResponseBody

/**
 * Returns a [BitLabsResponse] object converted from JSON.
 * @receiver The [error body][ResponseBody] of a response that received a status code in 400..500
 */
internal inline fun <reified T> ResponseBody.body(): BitLabsResponse<T>? =
    try {
        Moshi.Builder()
            .build()
            .adapter<BitLabsResponse<T>>(
                Types.newParameterizedType(
                    BitLabsResponse::class.java,
                    T::class.java
                )
            )
            .fromJson(this.string())
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        null
    }
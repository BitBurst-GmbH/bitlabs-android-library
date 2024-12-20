package ai.bitlabs.sdk.util.extensions

import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

/**
 * Returns a [BitLabsResponse] object converted from JSON.
 * @receiver The [error body][ResponseBody] of a response that received a status code in 400..500
 */
internal inline fun <reified T> ResponseBody.body(): BitLabsResponse<T>? =
    try {
        val type = object : TypeToken<BitLabsResponse<T>>() {}.type
        GsonBuilder().create().fromJson(this.string(), type)
    } catch (e: Exception) {
        SentryManager.captureException(e)
        Log.e(TAG, e.toString())
        null
    }
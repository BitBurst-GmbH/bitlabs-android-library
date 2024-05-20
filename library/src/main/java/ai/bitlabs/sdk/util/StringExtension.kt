package ai.bitlabs.sdk.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder


/**
 * Returns a [HookMessage] object converted from JSON.
 * @receiver The post message data string that received from the webview
 */
internal inline fun <reified T> String.hookMessage(): HookMessage<T>? =
    try {
        GsonBuilder().create().fromJson<HookMessage<T>>(this, HookMessage::class.java)
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        null
    }


/**
 * HookMessage data class that holds the message received from the webview.
 */
internal data class HookMessage<T>(
    val type: String,
    val name: String,
    val args: List<T>
)

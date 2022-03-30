package ai.bitlabs.sdk.data.model

import ai.bitlabs.sdk.WebActivity
import ai.bitlabs.sdk.util.TAG
import android.net.Uri
import android.util.Log

/**
 * This class holds the parameters of the [WebActivity] responsible to launch the OfferWall.
 * @constructor A Constructor that holds the values ([token], [uid], [tags])
 * which will be used in the [WebActivity] to launch the OfferWall correctly.
 */
internal data class WebActivityParams(
    private val token: String,
    private val uid: String,
    private val tags: Map<String, Any> = mapOf()
) {
    var url: String = ""
        get() = field.takeIf { it.isNotEmpty() } ?: buildUrl()
        private set

    /** Returns a string representation of the URL with all necessary parameters. */
    private fun buildUrl() = Uri
        .parse("https://web.bitlabs.ai").buildUpon()
        .appendQueryParameter("token", token)
        .appendQueryParameter("uid", uid)
        .appendQueryParameter("os", "ANDROID")
        .appendQueryParameter("sdk", "UNITY")
        .apply { tags.forEach { tag -> appendQueryParameter(tag.key, tag.value.toString()) } }
        .build()
        .toString()
        .also {
            url = it
            Log.i(TAG, url)
        }
}

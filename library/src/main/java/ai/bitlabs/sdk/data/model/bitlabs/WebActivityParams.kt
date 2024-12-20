package ai.bitlabs.sdk.data.model.bitlabs

import ai.bitlabs.sdk.views.BitLabsOfferwallActivity
import android.net.Uri
import androidx.annotation.Keep

/**
 * This class holds the parameters of the [BitLabsOfferwallActivity] responsible to launch the OfferWall.
 * @constructor A Constructor that holds the values ([token], [uid], [tags])
 * which will be used in the [BitLabsOfferwallActivity] to launch the OfferWall correctly.
 */
@Keep
internal data class WebActivityParams(
    private val token: String,
    private val uid: String,
    private val sdk: String,
    private val maid: String,
    private val tags: Map<String, Any> = mapOf()
) {
    var url: String = ""
        get() = field.takeIf { it.isNotEmpty() } ?: buildUrl()
        private set

    /** Returns a string representation of the URL with all necessary parameters. */
    private fun buildUrl() = Uri
        .parse("https://web.bitlabs.ai").buildUpon()
        .appendQueryParameter("os", "ANDROID")
        .appendQueryParameter("token", token)
        .appendQueryParameter("uid", uid)
        .appendQueryParameter("sdk", sdk)
        .apply { if (maid.isNotEmpty()) appendQueryParameter("maid", maid) }
        .apply { tags.forEach { tag -> appendQueryParameter(tag.key, tag.value.toString()) } }
        .build()
        .toString()
        .also { url = it }
}

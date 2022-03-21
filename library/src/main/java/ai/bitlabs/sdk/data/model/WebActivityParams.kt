package ai.bitlabs.sdk.data.model

import ai.bitlabs.sdk.util.LeaveSurveyListener
import android.net.Uri
import java.io.Serializable

internal data class WebActivityParams(
    private val token: String,
    private val uid: String,
    private val tags: MutableMap<String, Any> = mutableMapOf(),
    val leaveSurveyListener: LeaveSurveyListener
) : Serializable {
    private var url: String? = null

    fun getURL() = url ?: buildURL()

    private fun buildURL() =
        Uri.parse("https://web.bitlabs.ai").buildUpon()
            .appendQueryParameter("token", token)
            .appendQueryParameter("uid", uid)
            .apply { tags.forEach { tag -> appendQueryParameter(tag.key, tag.value.toString()) } }
            .build()
            .toString()
            .also { url = it }
}

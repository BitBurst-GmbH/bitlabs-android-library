package ai.bitlabs.sdk.data.model

import android.net.Uri
import retrofit2.http.GET
import java.io.Serializable

data class WebActivityParams(
    var token: String,
    var uid: String,
    var tags: MutableMap<String, String> = mutableMapOf()
) : Serializable {
    private var url: String = ""

    fun getURL() =
        url.takeIf { it.isNotEmpty() } ?: with(Uri.parse("https://web.bitlabs.ai").buildUpon()) {
            appendQueryParameter("token", token)
            appendQueryParameter("uid", uid)
            tags.forEach { tag -> appendQueryParameter(tag.key, tag.value) }
            url = build().toString()
            url
        }
}

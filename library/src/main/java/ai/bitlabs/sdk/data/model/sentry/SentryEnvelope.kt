package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody

interface SentryEnvelopeItem

data class SentryEnvelope(
    val headers: SentryEnvelopeHeaders,
    val items: List<SentryEnvelopeItem>
) {
    override fun toString(): String {
        val gson = Gson()

        val headersJson = gson.toJson(headers)
        val itemsJson = items.joinToString("") { it.toString() }


        return """
            $headersJson
            $itemsJson
        """.trimIndent()
    }

    fun toRequestBody(): RequestBody = RequestBody.create(
        MediaType.parse("application/x-sentry-envelope"),
        toString()
    )

}

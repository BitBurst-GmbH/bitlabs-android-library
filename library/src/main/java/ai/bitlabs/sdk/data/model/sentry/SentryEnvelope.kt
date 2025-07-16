package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    fun toRequestBody(): RequestBody = toString()
        .toRequestBody("application/x-sentry-envelope".toMediaTypeOrNull())

}

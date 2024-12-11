package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.Gson

data class SentryEventItem(val event: SentryEvent) : SentryEnvelopeItem {
    override fun toString(): String {
        val gson = Gson()

        val eventJson = gson.toJson(event)
        val itemHeadersJson = """
            {"type": "event", "length": ${eventJson.length}}
        """.trimIndent()

        return """
            $itemHeadersJson
            $eventJson
        """.trimIndent()
    }
}
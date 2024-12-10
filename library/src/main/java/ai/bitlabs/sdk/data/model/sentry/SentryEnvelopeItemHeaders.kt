package ai.bitlabs.sdk.data.model.sentry

data class SentryEnvelopeItemHeaders(
    val type: String,
    val length: Int? = null,
    val contentEncoding: String? = null,
    val contentType: String? = null
)

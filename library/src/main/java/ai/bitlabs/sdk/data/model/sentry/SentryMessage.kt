package ai.bitlabs.sdk.data.model.sentry

data class SentryMessage(
    val formatted: String,
    val message: String? = null,
    val params: List<String>? = null
)

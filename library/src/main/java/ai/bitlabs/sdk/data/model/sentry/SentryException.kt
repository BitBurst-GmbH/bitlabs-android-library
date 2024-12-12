package ai.bitlabs.sdk.data.model.sentry

data class SentryException(
    val type: String,
    val value: String,
    val module: String? = null,
    val stacktrace: SentryStackTrace? = null
)

package ai.bitlabs.sdk.data.model.sentry

data class SentryExceptionMechanism(
    val type: String = "generic",
    val handled: Boolean = true,
    val data: Map<String, String>? = null,
    val meta: Map<String, String>? = null
)

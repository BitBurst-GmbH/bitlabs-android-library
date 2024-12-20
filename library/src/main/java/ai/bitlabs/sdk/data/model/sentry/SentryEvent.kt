package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.annotations.SerializedName

data class SentryEvent(
    @SerializedName("event_id") val eventId: String,
    val timestamp: String,
    val logentry: SentryMessage? = null,
    val level: String? = null,
    val platform: String = "other",
    val logger: String? = null,
    @SerializedName("server_name") val serverName: String? = null,
    val release: String? = null,
    val environment: String? = null,
    val modules: Map<String, String>? = null,
    val extra: Map<String, String>? = null,
    val tags: Map<String, String>? = null,
    val fingerprint: List<String>? = null,
    val user: SentryUser?,
    val sdk: SentrySDK?,
    val exception: List<SentryException>? = null,
//    val exception: List<SentryException>,
//    val breadcrumbs: List<SentryBreadcrumb>
)

package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.annotations.SerializedName

data class SentryEnvelopeHeaders(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("sent_at") val sentAt: String,
    val dsn: String,
)

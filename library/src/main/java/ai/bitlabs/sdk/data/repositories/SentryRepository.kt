package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.data.api.SentryAPI
import ai.bitlabs.sdk.data.model.sentry.SentryEnvelope
import ai.bitlabs.sdk.data.model.sentry.SentryEnvelopeHeaders
import ai.bitlabs.sdk.data.model.sentry.SentryEvent
import ai.bitlabs.sdk.data.model.sentry.SentryEventItem
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.model.sentry.SentryMessage
import ai.bitlabs.sdk.data.model.sentry.SentrySDK
import ai.bitlabs.sdk.data.model.sentry.SentryUser
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import com.google.gson.Gson
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


internal class SentryRepository(
    private val sentryAPI: SentryAPI, private val token: String, private val uid: String
) {
    fun sendEnvelope() {
        val gson = Gson()

        val evenId = UUID.randomUUID().toString().replace("-", "")
        val now = with(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US)) {
            timeZone = TimeZone.getTimeZone("UTC")
            format(Date())
        }
        val event = SentryEvent(
            eventId = evenId,
            timestamp = now,
            logentry = SentryMessage("Another Test message"),
            user = SentryUser(id = uid),
            sdk = SentrySDK(version = "0.1.0")
        )

        val eventItem = SentryEventItem(event)

        val envelope = SentryEnvelope(
            headers = SentryEnvelopeHeaders(
                eventId = evenId, dsn = SentryManager.dsn.toString(), sentAt = now
            ),
            items = listOf(eventItem)
        ).toRequestBody()

        Log.i(TAG, "sendEnvelope: ${bodyToString(envelope)}")

        sentryAPI.sendEnvelope(envelope = envelope).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Log.i(
                        TAG, "onResponse: Sentry envelope sent. Response: ${
                            response.body()?.string()
                        }"
                    )
                } else {
                    Log.e(
                        TAG, "onResponse: Sentry envelope not sent. Error: ${
                            response.errorBody()?.string()
                        }"
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: Sentry envelope not sent", t)
            }
        })
    }

    private fun bodyToString(request: RequestBody): String {
        try {
            val buffer = Buffer()
            request.writeTo(buffer)
            return buffer.readUtf8()
        } catch (e: IOException) {
            return "did not work"
        }
    }
}
package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.data.api.SentryAPI
import ai.bitlabs.sdk.data.model.sentry.SentryEnvelope
import ai.bitlabs.sdk.data.model.sentry.SentryEnvelopeHeaders
import ai.bitlabs.sdk.data.model.sentry.SentryEvent
import ai.bitlabs.sdk.data.model.sentry.SentryEventItem
import ai.bitlabs.sdk.data.model.sentry.SentryException
import ai.bitlabs.sdk.data.model.sentry.SentryExceptionMechanism
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.model.sentry.SentryMessage
import ai.bitlabs.sdk.data.model.sentry.SentrySDK
import ai.bitlabs.sdk.data.model.sentry.SentryStackFrame
import ai.bitlabs.sdk.data.model.sentry.SentryStackTrace
import ai.bitlabs.sdk.data.model.sentry.SentryUser
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import com.google.gson.Gson
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.UncaughtExceptionHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors


internal class SentryRepository(
    private val sentryAPI: SentryAPI,
    private val token: String,
    private val uid: String,
    private val executor: Executor
) {

    fun sendEnvelope(
        throwable: Throwable, defaultUncaughtExceptionHandler: UncaughtExceptionHandler?
    ) {

        val envelope =
            createEnvelopeJson(throwable, isHandled = defaultUncaughtExceptionHandler == null)

        executor.execute {
            try {
                val response = sentryAPI.sendEnvelope(envelope = envelope).execute()

                if (response.isSuccessful) {
                    Log.i(TAG, "Sentry envelope(#${response.body()?.id}) sent.")
                } else {
                    Log.e(
                        TAG, "Sentry envelope not sent. Error: ${response.errorBody()?.string()}"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Sentry sendEnvelope Failed", e)
            }

            defaultUncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
        }
    }

    private fun createEnvelopeJson(throwable: Throwable, isHandled: Boolean): RequestBody {
        val gson = Gson()

        val evenId = UUID.randomUUID().toString().replace("-", "")
        val now = with(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US)) {
            timeZone = TimeZone.getTimeZone("UTC")
            format(Date())
        }

        val exception = SentryException(
            type = throwable.javaClass.simpleName,
            value = throwable.message ?: "Unlabelled exception",
            module = throwable.javaClass.simpleName,
            stacktrace = SentryStackTrace(frames = throwable.stackTrace.map {
                SentryStackFrame(
                    filename = it.fileName,
                    function = it.methodName,
                    module = it.className,
                    lineno = maxOf(it.lineNumber, 0),
                    inApp = it.className.startsWith("ai.bitlabs.sdk")
                )
            }),
            mechanism = SentryExceptionMechanism(handled = isHandled)
        )

        val event = SentryEvent(
            eventId = evenId,
            timestamp = now,
            logentry = SentryMessage(throwable.message ?: "Unlabelled exception"),
            user = SentryUser(id = uid),
            sdk = SentrySDK(version = "0.1.0"),
            exception = listOf(exception),
            tags = mapOf("token" to token),
            level = if (isHandled) "error" else "fatal"
        )

        val eventItem = SentryEventItem(event)

        return SentryEnvelope(
            headers = SentryEnvelopeHeaders(
                eventId = evenId, dsn = SentryManager.dsn.toString(), sentAt = now
            ), items = listOf(eventItem)
        ).toRequestBody()
    }
}
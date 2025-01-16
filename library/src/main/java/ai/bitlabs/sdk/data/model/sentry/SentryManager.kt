package ai.bitlabs.sdk.data.model.sentry

import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.data.api.SentryAPI
import ai.bitlabs.sdk.data.repositories.SentryRepository
import ai.bitlabs.sdk.util.buildHttpClientWithHeaders
import ai.bitlabs.sdk.util.buildRetrofit
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.Executors

internal object SentryManager {
    val dsn = SentryDsn(BuildConfig.DSN)

    val projectId = dsn.projectId

    private val host = dsn.host
    private val protocol = dsn.protocol
    private val publicKey = dsn.publicKey

    private val url = "$protocol://$host/"

    private var sentryRepo: SentryRepository? = null

    fun init(token: String, uid: String) {
        val okHttpClient = buildHttpClientWithHeaders(
            "X-Sentry-Auth" to "Sentry sentry_version=7, sentry_key=$publicKey, sentry_client=bitlabs-sdk/0.1.0",
            "User-Agent" to "bitlabs-sdk/0.1.0"
        )

        val retrofit = buildRetrofit(url, okHttpClient)

        sentryRepo = SentryRepository(
            retrofit.create(SentryAPI::class.java),
            token,
            uid,
            Executors.newSingleThreadExecutor()
        )
    }

    fun captureException(
        throwable: Throwable,
        defaultUncaughtExceptionHandler: UncaughtExceptionHandler? = null
    ) {
        sentryRepo?.sendEnvelope(throwable, defaultUncaughtExceptionHandler)
    }
}
package ai.bitlabs.sdk.data.model.sentry

import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.data.api.SentryAPI
import ai.bitlabs.sdk.data.repositories.SentryRepository
import ai.bitlabs.sdk.util.buildHttpClientWithHeaders
import ai.bitlabs.sdk.util.buildRetrofit

internal object SentryManager {
    val dsn = SentryDsn(BuildConfig.SENTRY_DSN)

    val projectId = dsn.projectId

    private val host = dsn.host
    private val protocol = dsn.protocol
    private val publicKey = dsn.publicKey

    private val url = "$protocol://$host/"

    private lateinit var sentryRepo: SentryRepository

    fun init(uid: String, token: String) {
        val okHttpClient = buildHttpClientWithHeaders(
            "X-Sentry-Auth" to "Sentry sentry_version=7, sentry_key=$publicKey"
        )

        val retrofit = buildRetrofit(url, okHttpClient)

        sentryRepo = SentryRepository(retrofit.create(SentryAPI::class.java), token, uid)
    }

    fun catchException(throwable: Throwable) {
        sentryRepo.sendEnvelope(throwable)
    }
}
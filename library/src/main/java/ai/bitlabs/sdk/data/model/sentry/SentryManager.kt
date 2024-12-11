package ai.bitlabs.sdk.data.model.sentry

import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.data.api.SentryAPI
import ai.bitlabs.sdk.data.repositories.SentryRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object SentryManager {
    val dsn = SentryDsn(BuildConfig.SENTRY_DSN)

    val projectId = dsn.projectId

    private val host = dsn.host
    private val protocol = dsn.protocol
    private val publicKey = dsn.publicKey

    private val url = "$protocol://$host/"

    private lateinit var sentryRepo: SentryRepository

    fun init(uid: String, token: String) {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(
                        "X-Sentry-Auth",
                        "Sentry sentry_version=7, sentry_key=$publicKey"
                    )
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        sentryRepo = SentryRepository(retrofit.create(SentryAPI::class.java), token, uid)
    }
}
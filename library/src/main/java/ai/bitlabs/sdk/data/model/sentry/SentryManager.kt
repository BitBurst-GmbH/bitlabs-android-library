package ai.bitlabs.sdk.data.model.sentry

import ai.bitlabs.sdk.BuildConfig

internal object SentryManager {
    val dsn = SentryDsn(BuildConfig.SENTRY_DSN)

    val host = dsn.host
    val protocol = dsn.protocol
    val publicKey = dsn.publicKey
    val projectId = dsn.projectId

    val sentryUrl = "$protocol://$host/api/$projectId/envelope/"
}
package ai.bitlabs.sdk.data.model.sentry

import ai.bitlabs.sdk.BuildConfig

data class SentrySDK(
    val name: String = "bitlabs.kotlin.android.${BuildConfig.FLAVOR}",
    val version: String = BuildConfig.VERSION_NAME
)

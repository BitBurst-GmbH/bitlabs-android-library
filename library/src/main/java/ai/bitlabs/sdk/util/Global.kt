package ai.bitlabs.sdk.util

import android.content.res.Configuration
import android.content.res.Resources
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun buildHttpClientWithHeaders(vararg headers: Pair<String, String>) =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .apply { headers.forEach { addHeader(it.first, it.second) } }
                .build()
            chain.proceed(request)
        }
        .build()

internal fun buildRetrofit(baseUrl: String, okHttpClient: OkHttpClient) =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

internal fun deviceType(): String {
    val isTablet =
        Resources.getSystem().configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
    return if (isTablet) "tablet" else "phone"
}

internal fun String.snakeToCamelCase() = lowercase()
    .split("_")
    .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    .replaceFirstChar { c -> c.lowercase() }

internal fun String.convertKeysToCamelCase() = Regex("\"([a-z]+(?:_[a-z]+)+)\":")
    .replace(this) { match -> match.groupValues[1].snakeToCamelCase().let { "\"$it\":" } }
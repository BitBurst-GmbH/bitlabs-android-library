package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.model.sentry.SentryManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.toColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.RoundingMode

internal const val TAG = "BitLabs"

internal const val BASE_URL = "https://api.bitlabs.ai/"

internal const val BUNDLE_KEY_HEADER_COLOR = "bundle-key-header-color"

internal const val BUNDLE_KEY_BACKGROUND_COLOR = "bundle-key-background-color"

internal const val BUNDLE_KEY_URL = "bundle-key-url"

internal fun getColorScheme(): String {
    val darkModeFlags =
        Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val isDarkMode = darkModeFlags == Configuration.UI_MODE_NIGHT_YES

    return if (isDarkMode) "DARK" else "LIGHT"
}

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


internal fun getLuminance(color: Int) =
    0.2126 * Color.red(color) + 0.7152 * Color.green(color) + 0.0722 * Color.blue(color)

/**
 * @param color - Can be in the form of a css linear-gradient or a single hex color
 * @return - An array of two colors. If the input is a single color, the array will contain the same two colors
 */
internal fun extractColors(color: String) =
    Regex("""linear-gradient\((\d+)deg,\s*(.+)\)""").find(color)?.run {
        groupValues[2].replace("([0-9]+)%".toRegex(), "")
            .split(",\\s".toRegex())
            .map { it.trim().toColorInt() }
            .toIntArray()
    } ?: Regex("""#([0-9a-fA-F]{6})""").find(color)?.run {
        intArrayOf(groupValues[0].toColorInt(), groupValues[0].toColorInt())
    } ?: intArrayOf()

internal fun String.snakeToCamelCase() = lowercase()
    .split("_")
    .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    .replaceFirstChar { c -> c.lowercase() }

internal fun String.convertKeysToCamelCase() = Regex("\"([a-z]+(?:_[a-z]+)+)\":")
    .replace(this) { match -> match.groupValues[1].snakeToCamelCase().let { "\"$it\":" } }

//internal fun Number.toPx() = TypedValue.applyDimension(
//    TypedValue.COMPLEX_UNIT_DIP,
//    this.toFloat(),
//    Resources.getSystem().displayMetrics
//)

internal fun String.rounded(): String {
    try {
        with(BigDecimal(this).setScale(2, RoundingMode.DOWN)) {
            return if (this.scale() == 0) toPlainString()
            else stripTrailingZeros().toPlainString()
        }
    } catch (e: NumberFormatException) {
        SentryManager.captureException(e)
        Log.e(TAG, "rounded: Tried to round non-number!", e)
        return this
    }
}

internal fun ImageView.setQRCodeBitmap(value: String) =
    createBitmap(512, 512, Bitmap.Config.RGB_565)
        .apply {
            val bitMtx = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 512, 512)
            for (x in 0 until 512)
                for (y in 0 until 512)
                    set(x, y, if (bitMtx.get(x, y)) Color.BLACK else Color.WHITE)
        }.let { setImageBitmap(it) }

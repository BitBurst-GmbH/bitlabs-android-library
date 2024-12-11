package ai.bitlabs.sdk.util

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.math.BigDecimal
import java.math.RoundingMode

internal const val TAG = "BitLabs"

internal const val BASE_URL = "https://api.bitlabs.ai/"

internal const val BUNDLE_KEY_COLOR = "bundle-key-color"

internal const val BUNDLE_KEY_URL = "bundle-key-url"

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
            .map { Color.parseColor(it.trim()) }
            .toIntArray()
    } ?: Regex("""#([0-9a-fA-F]{6})""").find(color)?.run {
        intArrayOf(Color.parseColor(groupValues[0]), Color.parseColor(groupValues[0]))
    } ?: intArrayOf()

internal fun String.snakeToCamelCase() = lowercase()
    .split("_")
    .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    .replaceFirstChar { c -> c.lowercase() }

internal fun String.convertKeysToCamelCase() = Regex("\"([a-z]+(?:_[a-z]+)+)\":")
    .replace(this) { match -> match.groupValues[1].snakeToCamelCase().let { "\"$it\":" } }

internal fun Number.toPx() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
)

internal fun String.rounded(): String {
    try {
        with(BigDecimal(this).setScale(2, RoundingMode.DOWN)) {
            return if (this.scale() == 0) toPlainString()
            else stripTrailingZeros().toPlainString()
        }
    } catch (e: NumberFormatException) {
        Log.e(TAG, "rounded: Tried to round non-number!", e)
        return this
    }
}

internal fun ImageView.setQRCodeBitmap(value: String) = Bitmap
    .createBitmap(512, 512, Bitmap.Config.RGB_565)
    .apply {
        val bitMtx = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 512, 512)
        for (x in 0 until 512)
            for (y in 0 until 512)
                setPixel(x, y, if (bitMtx.get(x, y)) Color.BLACK else Color.WHITE)
    }.let { setImageBitmap(it) }

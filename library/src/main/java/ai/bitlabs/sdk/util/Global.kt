package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.model.Category
import ai.bitlabs.sdk.data.model.Survey
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue

import kotlin.random.Random

internal const val TAG = "BitLabs"

internal const val BUNDLE_KEY_PARAMS = "bundle-key-params"

internal const val BUNDLE_KEY_COLOR = "bundle-key-color"

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
    } ?: intArrayOf(Color.parseColor(color), Color.parseColor(color))

internal fun randomSurvey(i: Int) = with(Random(i)) {
    Survey(
        id = i.toString(),
        cpi = "0.5",
        value = "500",
        loi = nextDouble(10.0),
        category = Category("Survey-$i", "", "", ""),
        rating = nextInt(6),
        country = "US",
        language = "en",
        tags = listOf("recontact", "pii"),
        type = "survey",
        clickUrl = "",
    )
}

internal fun String.snakeToCamelCase() = split("_")
    .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    .replaceFirstChar { c -> c.lowercase() }

internal fun String.convertKeysToCamelCase() = Regex("\"([a-z]+(?:_[a-z]+)+)\":")
    .replace(this) { match -> match.groupValues[1].snakeToCamelCase().let { "\"$it\":" } }

internal fun Number.toPx() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
)
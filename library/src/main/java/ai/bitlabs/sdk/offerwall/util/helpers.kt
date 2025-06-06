package ai.bitlabs.sdk.offerwall.util

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import androidx.core.graphics.toColorInt

internal fun getColorScheme(): String {
    val darkModeFlags =
        Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val isDarkMode = darkModeFlags == Configuration.UI_MODE_NIGHT_YES

    return if (isDarkMode) "dark" else "light"
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
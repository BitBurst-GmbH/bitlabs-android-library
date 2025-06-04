package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.offerwall.util.extractColors
import ai.bitlabs.sdk.offerwall.util.getLuminance
import android.graphics.Color
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@SmallTest
class GlobalKtTest {
    @Test
    fun getLuminance_ColorWhite_Returns255() {
        val whiteColor = Color.rgb(255, 255, 255)
        val expectedLuminance = 255.0

        val actualLuminance = getLuminance(whiteColor)
        assertThat(actualLuminance).isWithin(0.01).of(expectedLuminance)
    }

    @Test
    fun getLuminance_ColorBlack_Returns0() {
        val blackColor = Color.rgb(0, 0, 0)
        val expectedLuminance = 0.0

        val actualLuminance = getLuminance(blackColor)
        assertThat(actualLuminance).isWithin(0.01).of(expectedLuminance)
    }

    @Test
    fun extractColors_SingleHexColor_ReturnsTwoInstancesOfColor() {
        val hexColor = "#FFFFFF"
        val expectedColors = intArrayOf(Color.parseColor(hexColor), Color.parseColor(hexColor))

        val actualColors = extractColors(hexColor)
        assertThat(actualColors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_LinearGradient_ReturnsTwoGradientColors() {
        val gradientString = "linear-gradient(90deg, #FFFFFF 0%, #000000 100%)"
        val expectedColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#000000")
        )

        val actualColors = extractColors(gradientString)
        assertThat(actualColors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_EmptyString_ReturnsEmptyArray() {
        val emptyString = ""
        val expectedColors = intArrayOf()

        val actualColors = extractColors(emptyString)
        assertThat(actualColors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_InvalidString_ReturnsEmptyArray() {
        val invalidString = "Not a color or gradient"
        val expectedColors = intArrayOf()

        val actualColors = extractColors(invalidString)
        assertThat(actualColors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_InvalidHexString_ReturnsEmptyArray() {
        val invalidHexString = "#ZZZZZZ"
        val expectedColors = intArrayOf()

        val actualColors = extractColors(invalidHexString)
        assertThat(actualColors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_PartialGradient_ReturnsEmptyArray() {
        val partialGradientString = "linear-gradient(90deg, #FFFFFF 0%)"
        val expectedColors = intArrayOf(Color.parseColor("#FFFFFF"))

        val actualColors = extractColors(partialGradientString)
        assertThat(actualColors).isEqualTo(expectedColors)
    }
}
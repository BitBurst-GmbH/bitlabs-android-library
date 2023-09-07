package ai.bitlabs.sdk.util

import android.graphics.Color
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@SmallTest
class GlobalKtTest {

    @Test
    fun getLuminance_ColorWhite_255() {
        val color = Color.rgb(255, 255, 255).toInt()
        val expectedLuminance = 255.0

        val luminance = getLuminance(color)
        assertThat(luminance).isWithin(0.01).of(expectedLuminance)
    }

    @Test
    fun getLuminance_ColorBlack_0() {
        val color = Color.rgb(0, 0, 0).toInt()
        val expectedLuminance = 0.0

        val luminance = getLuminance(color)
        assertThat(luminance).isWithin(0.01).of(expectedLuminance)
    }

    @Test
    fun extractColors_SingleHexColor_TwoColors() {
        val color = "#FFFFFF"
        val expectedColors = intArrayOf(Color.parseColor(color), Color.parseColor(color))

        val colors = extractColors(color)
        assertThat(colors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_LinearGradient_TwoColors() {
        val color = "linear-gradient(90deg, #FFFFFF 0%, #000000 100%)"
        val expectedColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#000000")
        )

        val colors = extractColors(color)
        assertThat(colors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_EmptyString_EmptyArray() {
        val color = ""
        val expectedColors = intArrayOf()

        val colors = extractColors(color)
        assertThat(colors).isEqualTo(expectedColors)
    }

    @Test
    fun extractColors_NonColorOrGradient_EmptyArray() {
        val color = "A String that isn't a color or gradient"
        val expectedColors = intArrayOf()

        val colors = extractColors(color)
        assertThat(colors).isEqualTo(expectedColors)
    }
}
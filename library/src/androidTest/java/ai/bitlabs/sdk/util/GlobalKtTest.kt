package ai.bitlabs.sdk.util

import android.graphics.Color
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@SmallTest
class GlobalKtTest {

    @Test
    fun getLuminance_WhenColorWhite_Expect255() {
        val color = Color.rgb(255, 255, 255).toInt()
        val expectedLuminance = 255.0

        val luminance = getLuminance(color)
        assertThat(luminance).isWithin(0.01).of(expectedLuminance)
    }

    @Test
    fun getLuminance_WhenColorBlack_Expect0() {
        val color = Color.rgb(0, 0, 0).toInt()
        val expectedLuminance = 0.0

        val luminance = getLuminance(color)
        assertThat(luminance).isWithin(0.01).of(expectedLuminance)
    }
}
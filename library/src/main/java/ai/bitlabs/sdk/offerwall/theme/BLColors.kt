package ai.bitlabs.sdk.offerwall.theme

import ai.bitlabs.sdk.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

object BLColors {
    val Primary: Color
        @Composable
        get() = colorResource(id = R.color.colorPrimary)

    val PrimaryDark: Color
        @Composable
        get() = colorResource(id = R.color.colorPrimaryDark)

    val Accent: Color
        @Composable
        get() = colorResource(id = R.color.colorAccent)
}
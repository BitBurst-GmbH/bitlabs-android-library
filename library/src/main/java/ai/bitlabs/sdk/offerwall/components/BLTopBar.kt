package ai.bitlabs.sdk.offerwall.components

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.offerwall.theme.BLColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BLTopBar(headerColors: List<Color>, isColorBright: Boolean, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(brush = Brush.linearGradient(headerColors))
            .padding(horizontal = 16.dp)
            .testTag("BLTopBar")
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_circle_chevron_left_regular),
                contentDescription = "Back Button",
                modifier = Modifier
                    .clickable { onBackPressed() }
                    .size(24.dp),
                colorFilter = ColorFilter.tint(if (isColorBright) Color.Black else Color.White)
            )
            BasicText(
                text = "Offerwall",
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .align(alignment = Alignment.CenterVertically),
                style = TextStyle(
                    color = if (isColorBright) Color.Black else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Preview
@Composable
fun BLTopBarPreview() {
    BLTopBar(
        headerColors = listOf(BLColors.Primary, BLColors.Accent),
        isColorBright = false,
        onBackPressed = {}
    )
}
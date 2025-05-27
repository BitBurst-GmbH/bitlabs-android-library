package ai.bitlabs.sdk.offerwall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BLText(
    text: String,
    height: Dp? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    style: TextStyle = BLStyle.normal,
    contentAlignment: Alignment = Alignment.CenterStart,
) {
    var modifier = modifier.then(Modifier.fillMaxWidth())

    if (height != null) modifier = modifier.height(height)

    if (onClick != null) modifier = modifier.clickable { onClick() }

    Box(contentAlignment = contentAlignment, modifier = modifier) {
        BasicText(text = text, style = style)
    }
}
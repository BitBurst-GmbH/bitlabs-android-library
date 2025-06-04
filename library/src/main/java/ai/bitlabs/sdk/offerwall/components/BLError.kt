package ai.bitlabs.sdk.offerwall.components

import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import ai.bitlabs.sdk.offerwall.theme.BLStyle
import ai.bitlabs.sdk.offerwall.shared.BLText
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BLErrorQr(error: WebViewError) {
    Log.i(TAG, "BLErrorQr: ${error.toBitmap()}")

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = error.toBitmap().asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(75.dp)
        )
        BLText(
            text = error.encoded,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            style = BLStyle.normal,
        )
    }
}

@Preview
@Composable
fun BLErrorQrPreview() {
    BLErrorQr(
        WebViewError(url = "https://example.com")
    )
}
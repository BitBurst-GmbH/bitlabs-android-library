package ai.bitlabs.sdk.offerwall.components.photo_chooser

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.offerwall.TAG
import ai.bitlabs.sdk.offerwall.shared.BLText
import ai.bitlabs.sdk.offerwall.theme.BLColors
import ai.bitlabs.sdk.offerwall.theme.BLStyle
import android.Manifest
import android.net.Uri
import android.util.Log
import android.webkit.ValueCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File


@Composable
fun BLPhotoChooser(
    uriResult: ValueCallback<Array<Uri>>? = null,
    onDismiss: () -> Unit = {},
    token: String, // For Sentry
    uid: String, // For Sentry
) {
    var tempFile: File? = null
    val context = LocalContext.current

    var shouldShowPermissionDialog by remember { mutableStateOf(false) }

    val chooser = rememberLauncherForActivityResult(GetMultipleContents()) {
        uriResult?.onReceiveValue(it.toTypedArray())
        onDismiss()
    }

    val camera = rememberLauncherForActivityResult(TakePicture()) {
        if (tempFile != null) uriResult?.onReceiveValue(arrayOf(tempFile!!.toUri()))
        else uriResult?.onReceiveValue(null)
        onDismiss()
    }

    fun takePhoto() {
        try {
            tempFile = with(File(context.cacheDir, "bitlabs")) {
                if (exists()) delete()
                mkdir()
                File.createTempFile("temp_photo", ".jpg", this)
            }
            if (tempFile == null) throw Exception("Could not create tmp photo")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider.bitlabs",
                tempFile!!,
            )
            camera.launch(uri)
        } catch (e: Exception) {
            SentryManager.captureException(token, uid, e)
            Log.e(TAG, e.message, e)
        }
    }

    val permission = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) takePhoto()
        else shouldShowPermissionDialog = true
    }

    Dialog(onDismissRequest = { uriResult?.onReceiveValue(null); onDismiss() }) {
        Column(
            Modifier
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            BLText(stringResource(R.string.file_chooser_title), style = BLStyle.h6)
            Spacer(modifier = Modifier.height(16.dp))
            BLText(
                text = stringResource(R.string.file_chooser_camera),
                style = BLStyle.normal,
                height = 48.dp,
                onClick = { permission.launch(Manifest.permission.CAMERA) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BLText(
                text = stringResource(R.string.file_chooser_gallery),
                style = BLStyle.normal,
                height = 48.dp,
                onClick = { chooser.launch("image/*") }
            )
        }
    }

    if (shouldShowPermissionDialog) {
        BLPermissionDialog(onDismiss = { shouldShowPermissionDialog = false })
    }
}

@Preview
@Composable
private fun BLPhotoChooserPreview() {
    BLPhotoChooser(
        uriResult = null,
        onDismiss = {},
        token = "test_token",
        uid = "test_uid"
    )
}

@Preview
@Composable
private fun BLPermissionDialog(onDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            BLText("Permission Required", style = BLStyle.h6)
            Spacer(modifier = Modifier.height(16.dp))
            BLText(
                text = "Camera permission is required to take a photo. Please enable it in the app settings.",
                style = BLStyle.normal,
                height = 48.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            BLText(
                text = "OK",
                style = BLStyle.medium.copy(color = BLColors.Accent),
                height = 48.dp,
                onClick = { }
            )
        }
    }
}
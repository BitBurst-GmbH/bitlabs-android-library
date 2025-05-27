package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.util.extensions.setupClient
import ai.bitlabs.sdk.util.extensions.setupPostMessageHandler
import ai.bitlabs.sdk.util.extensions.setupSettings
import android.app.Activity
import android.content.pm.ActivityInfo
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BLWebView(token: String, url: String, listenerId: Int = 0) {
    val context = LocalContext.current

    val viewModel = remember { OfferwallViewModel(token, listenerId) }

    val isTopBarShown = remember { mutableStateOf(false) }
    val shouldShowLeaveSurveyDialog = remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            setupClient()
            setupSettings()
            setupPostMessageHandler(
                addReward = { viewModel.onSurveyReward(it) },
                setClickId = { viewModel.clickId = it ?: "" },
                toggleTopBar = { isTopBarShown.value = it },
            )
            loadUrl(url)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onOfferwallClosed() }
    }

    fun onBackPressed() {
        if (isTopBarShown.value) {
            shouldShowLeaveSurveyDialog.value = true
            return
        }

        if (webView.canGoBack() == true) {
            webView.goBack()
            return
        }

        (context as? Activity)?.finish()
    }

    if (shouldShowLeaveSurveyDialog.value) {
        LeaveSurveyDialog(
            onDismiss = { shouldShowLeaveSurveyDialog.value = false },
            leaveSurvey = { reason ->
                shouldShowLeaveSurveyDialog.value = false
                isTopBarShown.value = false

                webView.evaluateJavascript(
                    " window.history.go(-window.history.length + 1);", null
                )

                viewModel.leaveSurvey(reason)
            })
    }

    BackHandler { onBackPressed() }

    LaunchedEffect(isTopBarShown.value) {
        val activity = context as? Activity

        activity?.requestedOrientation =
            if (isTopBarShown.value) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        webView.apply {
            isScrollbarFadingEnabled = isTopBarShown.value
            settings.setSupportZoom(isTopBarShown.value)
            settings.builtInZoomControls = isTopBarShown.value
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (isTopBarShown.value) BLTopBar(
            viewModel.headerColors.value,
            ::onBackPressed
        )
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
    }
}
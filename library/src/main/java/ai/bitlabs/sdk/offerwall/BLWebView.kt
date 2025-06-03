package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import ai.bitlabs.sdk.util.extensions.setupChromeClient
import ai.bitlabs.sdk.util.extensions.setupClient
import ai.bitlabs.sdk.util.extensions.setupPostMessageHandler
import ai.bitlabs.sdk.util.extensions.setupSettings
import android.app.Activity
import android.content.pm.ActivityInfo
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat

@Composable
fun BLWebView(token: String, url: String, listenerId: Int = 0) {
    val context = LocalContext.current

    val viewModel = remember { OfferwallViewModel(token, listenerId) }

    val error = remember { mutableStateOf<WebViewError?>(null) }
    val isTopBarShown = remember { mutableStateOf(false) }
    val shouldShowLeaveSurveyDialog = remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            setupClient { error.value = it }
            setupSettings()
            setupChromeClient()
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

    BackHandler { onBackPressed() }

    val window = (LocalView.current.context as Activity).window
    SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = viewModel.isColorBright
            isAppearanceLightStatusBars = viewModel.isColorBright
        }
    }

    Column(Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(viewModel.headerColors.value.first()))
                .statusBarsPadding()
        )
        if (isTopBarShown.value) BLTopBar(
            viewModel.headerColors.value,
            viewModel.isColorBright,
            ::onBackPressed
        )
        AndroidView(factory = { webView }, modifier = Modifier.weight(1f))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(viewModel.backgroundColors.value.first()))
                .navigationBarsPadding()
        )
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

    if (error.value != null) {
        BLErrorQr(error.value!!)
    }
}
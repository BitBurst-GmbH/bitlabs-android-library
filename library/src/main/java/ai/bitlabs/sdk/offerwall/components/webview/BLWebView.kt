package ai.bitlabs.sdk.offerwall.components.webview

import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import ai.bitlabs.sdk.offerwall.components.BLErrorQr
import ai.bitlabs.sdk.offerwall.components.BLLeaveSurveyDialog
import ai.bitlabs.sdk.offerwall.components.BLTopBar
import ai.bitlabs.sdk.offerwall.components.photo_chooser.BLPhotoChooser
import ai.bitlabs.sdk.offerwall.util.setup
import ai.bitlabs.sdk.offerwall.util.setupPostMessageHandler
import ai.bitlabs.sdk.util.BLWebChromeClient
import ai.bitlabs.sdk.util.BLWebViewClient
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.webkit.ValueCallback
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat

@Composable
internal fun BLWebView(viewModel: BLWebViewViewModel, url: String) {
    val context = LocalContext.current

    var error by remember { mutableStateOf<WebViewError?>(null) }
    var isTopBarShown by remember { mutableStateOf(false) }
    var shouldShowLeaveSurveyDialog by remember { mutableStateOf(false) }
    var uriResult by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val webView = remember {
        WebView(context).apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            setup()
            webViewClient = BLWebViewClient { error = it }
            webChromeClient = BLWebChromeClient { uriResult = it }
            setupPostMessageHandler(
                addReward = { viewModel.onSurveyReward(it) },
                setClickId = { viewModel.clickId = it ?: "" },
                toggleTopBar = { isTopBarShown = it },
            )
            loadUrl(url)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onOfferwallClosed() }
    }

    LaunchedEffect(isTopBarShown) {
        val activity = context as? Activity

        activity?.requestedOrientation =
            if (isTopBarShown) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        webView.apply {
            isScrollbarFadingEnabled = isTopBarShown
            settings.setSupportZoom(isTopBarShown)
            settings.builtInZoomControls = isTopBarShown
        }
    }

    fun onBackPressed() {
        if (isTopBarShown) {
            shouldShowLeaveSurveyDialog = true
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
        if (isTopBarShown) BLTopBar(
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

    if (shouldShowLeaveSurveyDialog) {
        BLLeaveSurveyDialog(
            onDismiss = { shouldShowLeaveSurveyDialog = false },
            leaveSurvey = { reason ->
                shouldShowLeaveSurveyDialog = false
                isTopBarShown = false

                webView.evaluateJavascript(
                    " window.history.go(-window.history.length + 1);", null
                )

                viewModel.leaveSurvey(reason)
            })
    }

    if (error != null) {
        BLErrorQr(error!!)
    }

    if (uriResult != null) {
        BLPhotoChooser(
            uriResult,
            onDismiss = { uriResult = null },
        )
    }
}
package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extensions.setupClient
import ai.bitlabs.sdk.util.extensions.setupPostMessageHandler
import ai.bitlabs.sdk.util.extensions.setupSettings
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BLWebView(url: String) {
    val context = LocalContext.current
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val shouldShowTopBar = remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            setupClient()
            setupSettings()
            setupPostMessageHandler(
                addReward = {},
                setClickId = {},
                toggleTopBar = { shouldShowTopBar.value = it },
            )
            loadUrl(url)
        }
    }

    fun onBackPressed() {
        if (shouldShowTopBar.value) {
//                    showLeaveSurveyAlertDialog()
            return
        }

        if (webView.canGoBack() == true) {
            webView.goBack()
            return
        }

        (context as? Activity)?.finish()
    }

    BackHandler { onBackPressed() }

    LaunchedEffect(shouldShowTopBar.value) {
        val activity = context as? Activity

        activity?.requestedOrientation =
            if (shouldShowTopBar.value) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        webView.apply {
            isScrollbarFadingEnabled = shouldShowTopBar.value
            settings.setSupportZoom(shouldShowTopBar.value)
            settings.builtInZoomControls = shouldShowTopBar.value
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (shouldShowTopBar.value) BLTopBar(BuildConfig.APP_TOKEN, ::onBackPressed)
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
    }
}
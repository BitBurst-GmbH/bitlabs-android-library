package ai.bitlabs.sdk.offerwall

import ai.bitlabs.sdk.BitLabs
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.log

@Composable
fun BLWebView(url: String) {
    val context = LocalContext.current
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val viewModel = remember { OfferwallViewModel(BuildConfig.APP_TOKEN) }

    val isTopBarShown = remember { mutableStateOf(false) }
    val shouldShowLeaveSurveyDialog = remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            setupClient()
            setupSettings()
            setupPostMessageHandler(
                addReward = {},
                setClickId = { viewModel.clickId = it ?: "" },
                toggleTopBar = { isTopBarShown.value = it },
            )
            loadUrl(url)
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

    if (shouldShowLeaveSurveyDialog.value) {
        LeaveSurveyDialog(
            onDismiss = { shouldShowLeaveSurveyDialog.value = false },
            leaveSurvey = { reason ->
                Log.i(TAG, "BLWebView: LEFT WITH REASON $reason")
                shouldShowLeaveSurveyDialog.value = false
                isTopBarShown.value = false

                webView.evaluateJavascript(
                    " window.history.go(-window.history.length + 1);", null
                );

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
package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.bitlabs.WebViewError
import ai.bitlabs.sdk.views.BitLabsOfferwallActivity
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

/** Adds all necessary configurations for its receiver [BitLabsOfferwallActivity.webView] */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    addReward: (reward: Float) -> Unit,
    setClickId: (clickId: String?) -> Unit,
    onDoUpdateVisitedHistory: (isPageOfferWall: Boolean) -> Unit,
    onError: (error: WebViewError?, date: String, url: String) -> Unit,
) {
    var tempFile: File? = null
    var uriResult: ValueCallback<Array<Uri>>? = null

    val chooser = (context as BitLabsOfferwallActivity).registerForActivityResult(GetMultipleContents()) {
        uriResult?.onReceiveValue(it?.toTypedArray())
    }

    val camera = (context as BitLabsOfferwallActivity).registerForActivityResult(TakePicture()) {
        if (tempFile == null) uriResult?.onReceiveValue(null)
        uriResult?.onReceiveValue(arrayOf(tempFile!!.toUri()))
    }

    fun takePhoto() {
        try {
            tempFile = with(File(context.cacheDir, "bitlabs")) {
                if (exists()) delete()
                mkdir()
                File.createTempFile("temp_photo", ".jpg", this)
            }
            if (tempFile == null) throw Exception("Could not create tmp photo")
            val uri =
                FileProvider.getUriForFile(context, BitLabs.fileProviderAuthority, tempFile!!)
            camera.launch(uri)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    val permission =
        (context as BitLabsOfferwallActivity).registerForActivityResult(RequestPermission()) { granted ->
            if (granted) takePhoto()
            else AlertDialog.Builder(context)
                .setTitle("Permission required")
                .setMessage("Camera permission is required to take a photo. Please enable it in the app settings.")
                .setPositiveButton("OK") { _, _ -> }
                .setOnDismissListener { uriResult?.onReceiveValue(null) }
                .show()
        }

    if (Build.VERSION.SDK_INT >= 21) CookieManager.getInstance()
        .setAcceptThirdPartyCookies(this, true)
    else CookieManager.getInstance().setAcceptCookie(true)

    this.setLayerType(View.LAYER_TYPE_HARDWARE, null)

    this.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message
        ): Boolean {
            val newWebView = WebView(view.context)
            with(resultMsg.obj as WebView.WebViewTransport) { webView = newWebView }
            resultMsg.sendToTarget()

            newWebView.webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(
                    view: WebView?, url: String?, isReload: Boolean
                ) {
                    if (!url.isNullOrEmpty()) CustomTabsIntent.Builder().build()
                        .launchUrl(context, Uri.parse(url))
                    super.doUpdateVisitedHistory(view, url, isReload)
                }
            }

            return true
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            uriResult = filePathCallback

            AlertDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.file_chooser_title))
                .setItems(
                    arrayOf(
                        context.resources.getString(R.string.file_chooser_camera),
                        context.resources.getString(R.string.file_chooser_gallery)
                    )
                ) { _, which ->
                    if (which == 0) permission.launch(Manifest.permission.CAMERA)
                    else chooser.launch("image/*")
                }
                .setOnCancelListener { uriResult?.onReceiveValue(null) }
                .show()

            return true
        }
    }

    this.webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            onDoUpdateVisitedHistory(
                url?.startsWith("https://web.bitlabs.ai") ?: true
            )
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            view?.evaluateJavascript(
                """
            window.addEventListener('message', (event) => {
                window.AndroidWebView.postMessage(JSON.stringify(event.data));
            });
            """.trimIndent()
            ) {}
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceivedHttpError(
            view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
        ) {
            if (BitLabs.debugMode) onError(
                WebViewError(errorResponse = errorResponse),
                System.currentTimeMillis().toString(),
                request?.url.toString()
            )

            super.onReceivedHttpError(view, request, errorResponse)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            if (BitLabs.debugMode || error?.description?.contains("ERR_CLEARTEXT_NOT_PERMITTED") == true) onError(
                WebViewError(error = error),
                System.currentTimeMillis().toString(),
                request?.url.toString()
            )
            super.onReceivedError(view, request, error)
        }
    }

    this.addJavascriptInterface(object {
        @JavascriptInterface
        fun postMessage(message: String) {
            val hookMessage = message.asHookMessage() ?: return

            if (hookMessage.type != "hook") return;

            when (hookMessage.name) {
                HookName.SDK_CLOSE -> {
                    (context as BitLabsOfferwallActivity).finish()
                }

                HookName.SURVEY_START -> {
                    val clickId =
                        hookMessage.args.filterIsInstance<SurveyStartArgs>().firstOrNull()?.clickId
                    setClickId(clickId)
                    Log.i(TAG, "Caught Survey Start event with clickId: $clickId")
                }

                HookName.SURVEY_COMPLETE -> {
                    val reward =
                        hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()?.reward ?: 0f
                    addReward(reward)
                    Log.i(TAG, "Caught Survey Complete event with reward: $reward")
                }

                HookName.SURVEY_SCREENOUT -> {
                    val reward =
                        hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()?.reward ?: 0f
                    addReward(reward)
                    Log.i(TAG, "Caught Survey Screenout with reward: $reward")
                }

                HookName.SURVEY_START_BONUS -> {
                    val reward =
                        hookMessage.args.filterIsInstance<RewardArgs>().firstOrNull()?.reward ?: 0f
                    addReward(reward)
                    Log.i(TAG, "Caught Survey Start Bonus event with reward: $reward")
                }

                HookName.INIT -> {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            this@setup.evaluateJavascript(
                                """
                                window.parent.postMessage({ target: 'app.behaviour.close_button_visible', value: true });
                                """.trimIndent()
                            ) {}
                        }, 1000
                    )
                }
            }
        }
    }, "AndroidWebView")

    this.settings.run {
        databaseEnabled = true
        allowFileAccess = true
        javaScriptEnabled = true
        domStorageEnabled = true
        displayZoomControls = false
        setSupportMultipleWindows(true)
        cacheMode = WebSettings.LOAD_NO_CACHE
        mediaPlaybackRequiresUserGesture = false
        javaScriptCanOpenWindowsAutomatically = true
    }
}

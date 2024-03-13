package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WebViewError
import ai.bitlabs.sdk.views.WebActivity
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
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

/** Adds all necessary configurations for its receiver [WebActivity.webView] */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setup(
    onDoUpdateVisitedHistory: (isPageOfferWall: Boolean, url: String) -> Unit,
    onError: (error: WebViewError?, date: String, url: String) -> Unit,
) {
    var tempFile: File? = null
    var uriResult: ValueCallback<Array<Uri>>? = null

    val chooser = (context as WebActivity).registerForActivityResult(GetMultipleContents()) {
        uriResult?.onReceiveValue(it?.toTypedArray())
    }

    val camera = (context as WebActivity).registerForActivityResult(TakePicture()) {
        if (tempFile == null) uriResult?.onReceiveValue(null)
        uriResult?.onReceiveValue(arrayOf(tempFile!!.toUri()))
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
                    if (which == 0) takePhoto()
                    else chooser.launch("image/*")
                }
                .show()

            return true
        }

        private fun takePhoto() {
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
    }

    this.webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            onDoUpdateVisitedHistory(
                url?.startsWith("https://web.bitlabs.ai") ?: true, url ?: ""
            )
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceivedHttpError(
            view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
        ) {

            val isPageMagicReceipts =
                request?.url?.toString()?.contains("/heap-undefined.js") == true

            if (BitLabs.debugMode || (!isPageMagicReceipts && errorResponse?.statusCode == 404)) onError(
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
            Log.d(TAG, "onReceivedError: ${error?.description}")
            if (BitLabs.debugMode || error?.description?.contains("ERR_CLEARTEXT_NOT_PERMITTED") == true) onError(
                WebViewError(error = error),
                System.currentTimeMillis().toString(),
                request?.url.toString()
            )
            super.onReceivedError(view, request, error)
        }
    }

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

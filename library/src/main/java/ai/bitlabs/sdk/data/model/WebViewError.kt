package ai.bitlabs.sdk.data.model

import android.os.Build
import android.os.Build.VERSION
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse

class WebViewError(
    private val error: WebResourceError? = null,
    private val errorResponse: WebResourceResponse? = null
) {
    fun getStatusCode(): Int {
        var code = 0

        if( error != null && VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            code = error.errorCode
        } else if (errorResponse != null && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            code = errorResponse.statusCode
        }

        return code
    }

    fun getDescription(): String {
        var description = ""

        if( error != null && VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            description = error.description.toString()
        } else if (errorResponse != null && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            description = errorResponse.reasonPhrase
        }

        return description
    }
}
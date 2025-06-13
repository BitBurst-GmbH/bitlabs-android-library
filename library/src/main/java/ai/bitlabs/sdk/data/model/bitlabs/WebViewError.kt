package ai.bitlabs.sdk.data.model.bitlabs

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
import android.util.Base64
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class WebViewError(
    private val url: String,
    private val error: WebResourceError? = null,
    private val errorResponse: WebResourceResponse? = null,
) {
    private val statusCode: Int
        get() =
            if (error != null && VERSION.SDK_INT >= Build.VERSION_CODES.M) error.errorCode
            else errorResponse?.statusCode ?: 0

    private val description: String
        get() =
            if (error != null && VERSION.SDK_INT >= Build.VERSION_CODES.M) error.description.toString()
            else errorResponse?.reasonPhrase ?: ""

    val encoded: String
        get() = toString().toByteArray().let { Base64.encodeToString(it, Base64.DEFAULT) }

    fun toBitmap() = createBitmap(512, 512, Bitmap.Config.RGB_565).also {
        val bitMtx = QRCodeWriter()
            .encode(toString(), BarcodeFormat.QR_CODE, 512, 512)

        for (x in 0 until 512)
            for (y in 0 until 512)
                it[x, y] = if (bitMtx.get(x, y)) Color.BLACK else Color.WHITE
    }

    override fun toString(): String = """
   {code: $statusCode, description: $description, url: $url, date: ${System.currentTimeMillis()}}
   """.trimIndent()
}
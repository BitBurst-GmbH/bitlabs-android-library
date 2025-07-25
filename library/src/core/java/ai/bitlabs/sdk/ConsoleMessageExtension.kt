package ai.bitlabs.sdk

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ConsoleMessage.MessageLevel


private const val TAG = "BitLabs.Console"

fun ConsoleMessage.log() {
    val message = "${message()} -- From line ${lineNumber()} of ${sourceId()}"

    when (messageLevel()) {
        MessageLevel.DEBUG -> Log.d(TAG, message)
        MessageLevel.ERROR -> Log.e(TAG, message)
        MessageLevel.LOG -> Log.i(TAG, message)
        MessageLevel.TIP -> Log.i(TAG, message)
        MessageLevel.WARNING -> Log.w(TAG, message)
        null -> Log.i(TAG, message)
    }
}
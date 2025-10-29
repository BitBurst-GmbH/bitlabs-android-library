package ai.bitlabs.sdk.util

/**
 * Unity-specific callback interfaces without generics to avoid JNI type erasure issues.
 * These are concrete implementations that Unity's AndroidJavaProxy can properly implement.
 */

fun interface OnInitResponseListener {
    fun onResponse()
}

fun interface OnBooleanResponseListener {
    fun onResponse(response: Boolean)
}

fun interface OnStringResponseListener {
    fun onResponse(response: String)
}

package ai.bitlabs.sdk.data.model.sentry

import com.google.gson.annotations.SerializedName

data class SentryStackFrame(
    val filename: String? = null,
    val function: String? = null,
    val module: String? = null,
    val lineno: Int? = null,
    val colno: Int? = null,
    @SerializedName("abs_path") val absPath: String? = null,
    @SerializedName("context_line") val contextLine: String? = null,
    @SerializedName("pre_context") val preContext: List<String>? = null,
    @SerializedName("post_context") val postContext: List<String>? = null,
    @SerializedName("in_app") val inApp: Boolean? = null
)

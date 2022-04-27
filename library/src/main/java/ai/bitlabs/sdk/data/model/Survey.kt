package ai.bitlabs.sdk.data.model


import ai.bitlabs.sdk.WebActivity
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import android.content.Context
import android.content.Intent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Survey(
    @Json(name = "network_id") val networkId: Int,
    val id: Int,
    val cpi: String,
    val value: String,
    val loi: Double,
    val remaining: Int,
    val details: Details,
    val rating: Int,
    val link: String,
    @Json(name = "missing_questions") val missingQuestions: Int
) {
    fun open(context: Context) {
        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_PARAMS, link)
            context.startActivity(this)
        }
    }
}
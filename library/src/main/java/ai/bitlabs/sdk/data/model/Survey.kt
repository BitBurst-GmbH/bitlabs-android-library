package ai.bitlabs.sdk.data.model


import ai.bitlabs.sdk.BitLabs
import android.content.Context
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
    @Json(name = "missing_questions") val missingQuestions: Int?
) {
    fun open(context: Context) = BitLabs.launchOfferWall(context)
}
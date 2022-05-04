package ai.bitlabs.sdk.data.model


import ai.bitlabs.sdk.BitLabs
import android.content.Context
import com.squareup.moshi.Json

/**
 * Represents the survey the user can take.
 * @property[networkId]
 * @property[id] The id of the Survey.
 * @property[cpi] CPI of this survey in USD without any formatting applied.
 * @property[value] CPI formatted according to your app settings. Can be shown to the user directly.
 * @property[loi] Assumed length of the survey in minutes
 * @property[remaining] Amount of users that can still open the survey
 * @property[details] See [Details].
 * @property[rating] Difficulty ranking of this survey. 1-5 (1 = hard, 5 = easy). Minimum value is 1, maximum value is 5.
 * @property[link] This link can be used as is to open the survey. All relevant details are inserted on the server.
 * @property[missingQuestions] The amount of questions that have to be answered before the survey is guaranteed to be openable by the user.
 */
data class Survey(
    @field:Json(name = "network_id") val networkId: Int,
    val id: Int,
    val cpi: String,
    val value: String,
    val loi: Double,
    val remaining: Int,
    val details: Details,
    val rating: Int,
    val link: String,
    @field:Json(name = "missing_questions") val missingQuestions: Int?
) {
    fun open(context: Context) = BitLabs.launchOfferWall(context)
}
package ai.bitlabs.sdk.data.model


import ai.bitlabs.sdk.BitLabs
import android.content.Context
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Represents the survey the user can take.
 * @property[id] The id of the Survey.
 * @property[type] Values are **survey** or **start_bonus**.
 * @property[cpi] CPI of this survey in USD without any formatting applied.
 * @property[value] CPI formatted according to your app settings. Can be shown to the user directly.
 * @property[loi] Assumed length of the survey in minutes
 * @property[country] ISO 3166-1 ALPHA-2
 * @property[language] ISO 639-1
 * @property[rating] Difficulty ranking of this survey. 1-5 (1 = hard, 5 = easy). Minimum value is 1, maximum value is 5.
 * @property[tags] Values are **recontact** or **pii**. The tag **recontact** means that this is a follow-up survey for
 * specific users that completed a different survey before; The tag **pii** means that this survey might collect sensitive information from the user;
 */
@Keep
data class Survey(
    val id: String,
    val type: String,
    @SerializedName("click_url") val clickUrl: String,
    val cpi: String,
    val value: String,
    val loi: Double,
    val country: String,
    val language: String,
    val rating: Int,
    val category: Category,
    val tags: List<String>,
) {
    fun open(context: Context) = BitLabs.launchOfferWall(context)
}
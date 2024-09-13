package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Visual(
    @SerializedName("background_color") val backgroundColor: String,
    @SerializedName("color_rating_threshold") val colorRatingThreshold: Int,
    @SerializedName("custom_logo_url") val customLogoUrl: String,
    @SerializedName("element_border_radius") val elementBorderRadius: String,
    @SerializedName("hide_reward_value") val hideRewardValue: Boolean,
    @SerializedName("interaction_color") val interactionColor: String,
    @SerializedName("navigation_color") val navigationColor: String,
    @SerializedName("offerwall_width") val offerwallWidth: String,
    @SerializedName("screenout_reward") val screenoutReward: String,
    @SerializedName("survey_icon_color") val surveyIconColor: String
)
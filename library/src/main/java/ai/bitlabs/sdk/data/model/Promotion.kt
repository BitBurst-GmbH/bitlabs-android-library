package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class Promotion(
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("bonus_percentage") val bonusPercentage: Int,
)

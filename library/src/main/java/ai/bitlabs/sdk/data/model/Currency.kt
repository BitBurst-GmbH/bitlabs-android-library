package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class Currency(
    @SerializedName("bonus_percentage") val bonusPercentage: Int,
    @SerializedName("currency_promotion") val currencyPromotion: Int,
    val factor: String,
    @SerializedName("floor_decimal") val floorDecimal: Boolean,
    val symbol: Symbol
)
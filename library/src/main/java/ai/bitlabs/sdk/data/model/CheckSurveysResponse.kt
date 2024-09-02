package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class CheckSurveysResponse(
    @SerializedName("has_surveys") val hasSurveys: Boolean
)
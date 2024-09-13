package ai.bitlabs.sdk.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateClickBody(
    @SerializedName("leave_survey") val leaveSurvey: LeaveReason
)

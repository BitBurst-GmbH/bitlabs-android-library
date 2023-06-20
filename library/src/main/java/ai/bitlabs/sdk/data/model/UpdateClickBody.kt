package ai.bitlabs.sdk.data.model

import com.google.gson.annotations.SerializedName

data class UpdateClickBody(
    @SerializedName("leave_survey") val leaveSurvey: LeaveReason
)

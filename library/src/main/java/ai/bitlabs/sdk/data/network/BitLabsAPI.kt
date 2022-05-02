package ai.bitlabs.sdk.data.network

import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.CheckSurveysResponse
import ai.bitlabs.sdk.data.model.GetActionsResponse
import ai.bitlabs.sdk.data.model.LeaveReason
import retrofit2.Call
import retrofit2.http.*

/**
 * Responsible for communication with the BitLabs API
 */
internal interface BitLabsAPI {
    @GET("client/check?platform=MOBILE")
    fun checkSurveys(): Call<BitLabsResponse<CheckSurveysResponse>>

    @POST("client/networks/{networkId}/surveys/{surveyId}/leave")
    fun leaveSurvey(
        @Path("networkId") networkId: String,
        @Path("surveyId") surveyId: String,
        @Body leaveReason: LeaveReason
    ): Call<BitLabsResponse<Unit>>

    @GET("client/actions?platform=MOBILE&os=ANDROID")
    fun getActions(@Query("sdk") sdk: String): Call<BitLabsResponse<GetActionsResponse>>
}
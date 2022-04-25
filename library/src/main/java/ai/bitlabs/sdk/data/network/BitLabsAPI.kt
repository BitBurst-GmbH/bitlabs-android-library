package ai.bitlabs.sdk.data.network

import ai.bitlabs.sdk.data.model.*
import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.CheckSurveysResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("client/actions?platform=MOBILE")
    fun getActions(): Call<BitLabsResponse<GetActionsResponse>>
}
package ai.bitlabs.sdk.data.api

import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetLeaderboardResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Responsible for communication with the BitLabs API
 */
internal interface BitLabsAPI {
    @POST("/v2/client/clicks/{clickId}")
    fun updateClick(
        @Path("clickId") clickId: String,
        @Body click: UpdateClickBody
    ): Call<BitLabsResponse<Unit>>

    @GET("v2/client/surveys?platform=MOBILE&os=ANDROID")
    fun getSurveys(@Query("sdk") sdk: String): Call<BitLabsResponse<GetSurveysResponse>>

    @GET("v1/client/settings/v2")
    fun getAppSettings(@Query("color_scheme") colorScheme: String): Call<BitLabsResponse<GetAppSettingsResponse>>

    @GET("v1/client/leaderboard")
    fun getLeaderboard(): Call<BitLabsResponse<GetLeaderboardResponse>>
}
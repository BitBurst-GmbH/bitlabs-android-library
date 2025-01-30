package ai.bitlabs.sdk.data.api

import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetLeaderboardResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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

    @GET
    fun getCurrencyIcon(@Url url: String): Call<ResponseBody>
}
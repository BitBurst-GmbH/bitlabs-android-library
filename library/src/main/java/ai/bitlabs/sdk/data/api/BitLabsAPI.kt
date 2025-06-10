package ai.bitlabs.sdk.data.api

import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Responsible for communication with the BitLabs API
 */
internal interface BitLabsAPI {
    @POST("/v2/client/clicks/{clickId}")
    fun updateClick(
        @Path("clickId") clickId: String,
        @Body click: UpdateClickBody,
    ): Call<BitLabsResponse<Unit>>

    @GET("v2/client/surveys?platform=MOBILE&os=ANDROID")
    fun getSurveys(@Query("sdk") sdk: String): Call<BitLabsResponse<GetSurveysResponse>>

    @GET
    fun getAppSettings(@Url url: String): Call<GetAppSettingsResponse>
}
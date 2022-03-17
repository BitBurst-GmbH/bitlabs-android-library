package ai.bitlabs.sdk.data.network

import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.LeaveReason
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface BitLabsAPI {

    @GET("client/check?platform=MOBILE")
    fun checkSurveys(): Call<BitLabsResponse>

    @POST("client/networks/{networkId}/surveys/{surveyId}/leave")
    fun leaveSurvey(
        @Path("networkId") networkId: String,
        @Path("surveyId") surveyId: String,
        @Body leaveReason: LeaveReason
    ): Call<BitLabsResponse>

    companion object {
        private var instance: BitLabsAPI? = null
        private var token = ""
        private var uid = ""

        internal fun setup(token: String, uid: String) {
            this.token = token
            this.uid = uid
        }

        internal operator fun invoke(): BitLabsAPI = instance ?: Retrofit.Builder()
            .baseUrl("https://api.bitlabs.ai/v1/")
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                chain.run {
                    proceed(
                        request().newBuilder()
                            .addHeader("X-Api-Token", token)
                            .addHeader("X-User-Id", uid)
                            .build()
                    )
                }
            }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BitLabsAPI::class.java)

    }
}
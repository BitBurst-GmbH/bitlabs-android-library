package ai.bitlabs.sdk.data.network

import ai.bitlabs.sdk.data.model.BitLabsResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface BitLabsAPI {

    @GET("client/check?platform=MOBILE")
    fun checkSurveys(
        @Header("X-Api-Token") token: String,
        @Header("X-User-Id") id: String
    ): Call<BitLabsResponse>

    companion object {
        private var instance: BitLabsAPI? = null

        operator fun invoke(): BitLabsAPI = instance ?: Retrofit.Builder()
            .baseUrl("https://api.bitlabs.ai/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BitLabsAPI::class.java)

    }
}
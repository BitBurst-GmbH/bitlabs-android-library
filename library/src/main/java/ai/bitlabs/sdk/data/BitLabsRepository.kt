package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.*
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.randomSurvey
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(token: String, uid: String) {
    private val bitLabsAPI = Retrofit.Builder()
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
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(BitLabsAPI::class.java)

    internal fun checkSurveys(onResponseListener: OnResponseListener<Boolean>) =
        bitLabsAPI.checkSurveys().enqueue(object : Callback<BitLabsResponse> {
            override fun onResponse(
                call: Call<BitLabsResponse>,
                response: Response<BitLabsResponse>
            ) {
                if (response.isSuccessful) {
                    onResponseListener.onResponse(response.body()?.data?.has_surveys)
                } else {
                    response.errorBody()?.body()?.error?.details?.run {
                        Log.e(TAG, "CheckSurvey $http - $msg")
                    }
                    onResponseListener.onResponse(null)
                }
            }

            override fun onFailure(call: Call<BitLabsResponse>, t: Throwable) {
                Log.e(TAG, "CheckSurvey Failure - ${t.message ?: "Unknown Error"}")
                onResponseListener.onResponse(null)
            }
        })

    internal fun leaveSurvey(
        networkId: String,
        surveyId: String,
        reason: String,
        onResponseListener: OnResponseListener<Unit>
    ) = bitLabsAPI.leaveSurvey(networkId, surveyId, LeaveReason(reason))
        .enqueue(object : Callback<BitLabsResponse> {
            override fun onResponse(
                call: Call<BitLabsResponse>,
                response: Response<BitLabsResponse>
            ) {
                if (response.isSuccessful)
                    onResponseListener.onResponse(Unit)
                else {
                    response.errorBody()?.body()?.error?.details?.run {
                        Log.e(TAG, "LeaveSurvey $http - $msg")
                    }
                    onResponseListener.onResponse(null)
                }
            }

            override fun onFailure(call: Call<BitLabsResponse>, t: Throwable) {
                Log.e(TAG, "LeaveSurvey Failure - ${t.message ?: "Unknown Error"}")
                onResponseListener.onResponse(null)
            }
        })

    internal fun getSurveys(onResponseListener: OnResponseListener<List<Survey>>) =
        bitLabsAPI.getActions().enqueue(object : Callback<GetActionsResponse> {
            override fun onResponse(
                call: Call<GetActionsResponse>,
                response: Response<GetActionsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val surveys = response.body()!!.data.surveys.ifEmpty {
                        (1..3).map { randomSurvey(it) }
                    }
                    onResponseListener.onResponse(surveys)
                } else {
                    response.errorBody()?.body()?.error?.details?.run {
                        Log.e(TAG, "GetSurveys $http - $msg")
                    }
                    onResponseListener.onResponse(null)
                }
            }

            override fun onFailure(call: Call<GetActionsResponse>, t: Throwable) {
                Log.e(TAG, "GetSurveys Failure - ${t.message ?: "Unknown Error"}")
                onResponseListener.onResponse(null)
            }
        })
}
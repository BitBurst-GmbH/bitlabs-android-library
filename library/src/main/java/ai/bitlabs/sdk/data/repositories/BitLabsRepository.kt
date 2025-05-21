package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetLeaderboardResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.LeaveReason
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extensions.body
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(private val bitLabsAPI: BitLabsAPI) {
    internal fun leaveSurvey(clickId: String, reason: String) =
        bitLabsAPI.updateClick(clickId, UpdateClickBody(LeaveReason(reason)))
            .enqueue(object : Callback<BitLabsResponse<Unit>> {
                override fun onResponse(
                    call: Call<BitLabsResponse<Unit>>, response: Response<BitLabsResponse<Unit>>
                ) {
                    if (response.isSuccessful) Log.i(TAG, "LeaveSurvey - Success")
                    else response.errorBody()?.body<Unit>()?.error?.details?.run {
                        val errMessage = "LeaveSurvey Error: $http - $msg"
                        SentryManager.captureException(Exception(errMessage))
                        Log.e(TAG, errMessage)
                    }
                }

                override fun onFailure(call: Call<BitLabsResponse<Unit>>, t: Throwable) {
                    SentryManager.captureException(t)
                    Log.e(TAG, "LeaveSurvey Failure - ${t.message ?: "Unknown Error"}")
                }
            })

    internal fun getSurveys(
        sdk: String,
        onResponseListener: OnResponseListener<List<Survey>>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getSurveys(sdk).enqueue(object : Callback<BitLabsResponse<GetSurveysResponse>> {
        override fun onResponse(
            call: Call<BitLabsResponse<GetSurveysResponse>>,
            response: Response<BitLabsResponse<GetSurveysResponse>>
        ) {
            val restrictionReason = response.body()?.data?.restrictionReason
            if (restrictionReason != null) {
                with(Exception("GetSurveys Error: ${restrictionReason.prettyPrint()}")) {
                    SentryManager.captureException(this)
                    onExceptionListener.onException(this)
                }
                return
            }

            val surveys = response.body()?.data?.surveys ?: emptyList()
            if (surveys.isNotEmpty()) {
                onResponseListener.onResponse(surveys)
                return
            }

            response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
                with(Exception("GetSurveys Error: $http - $msg")) {
                    SentryManager.captureException(this)
                    onExceptionListener.onException(this)
                }
            }
        }

        override fun onFailure(call: Call<BitLabsResponse<GetSurveysResponse>>, t: Throwable) {
            SentryManager.captureException(t)
            onExceptionListener.onException(Exception(t))
        }
    })

    internal fun getAppSettings(
        colorScheme: String,
        onResponseListener: OnResponseListener<GetAppSettingsResponse>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getAppSettings(colorScheme)
        .enqueue(object : Callback<BitLabsResponse<GetAppSettingsResponse>> {
            override fun onResponse(
                call: Call<BitLabsResponse<GetAppSettingsResponse>>,
                response: Response<BitLabsResponse<GetAppSettingsResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { onResponseListener.onResponse(it) }
                    return
                }

                response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
                    with(Exception("GetAppSettings Error: $http - $msg")) {
                        SentryManager.captureException(this)
                        onExceptionListener.onException(this)
                    }
                }
            }

            override fun onFailure(
                call: Call<BitLabsResponse<GetAppSettingsResponse>>, t: Throwable
            ) {
                with(Exception(t)) {
                    SentryManager.captureException(this)
                    onExceptionListener.onException(this)
                }
            }
        })

    internal fun getLeaderboard(
        onResponseListener: OnResponseListener<GetLeaderboardResponse>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getLeaderboard()
        .enqueue(object : Callback<BitLabsResponse<GetLeaderboardResponse>> {
            override fun onResponse(
                call: Call<BitLabsResponse<GetLeaderboardResponse>>,
                response: Response<BitLabsResponse<GetLeaderboardResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { onResponseListener.onResponse(it) }
                    return
                }

                response.errorBody()?.body<GetLeaderboardResponse>()?.error?.details?.run {
                    with(Exception("GetLeaderboard Error: $http - $msg")) {
                        SentryManager.captureException(this)
                        onExceptionListener.onException(this)
                    }
                }
            }

            override fun onFailure(
                call: Call<BitLabsResponse<GetLeaderboardResponse>>, t: Throwable
            ) {
                with(Exception(t)) {
                    SentryManager.captureException(this)
                    onExceptionListener.onException(this)
                }
            }
        })
}
package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.BuildConfig
import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.LeaveReason
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.util.body
import ai.bitlabs.sdk.util.BASE_URL
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.buildHttpClientWithHeaders
import ai.bitlabs.sdk.util.buildRetrofit
import ai.bitlabs.sdk.util.deviceType
import android.os.Build
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** This class is the point of communication between the data and [BitLabs] */
object BitLabsRepository {

    private val bitLabsAPI: BitLabsAPI by lazy {
        val userAgent =
            "BitLabs/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.SDK_INT}; ${Build.MODEL}; ${deviceType()})"

        val okHttpClient = buildHttpClientWithHeaders("User-Agent" to userAgent)

        val retrofit = buildRetrofit(BASE_URL, okHttpClient)
        retrofit.create(BitLabsAPI::class.java)
    }

    internal fun leaveSurvey(token: String, uid: String, clickId: String, reason: String) {
        val headers = Pair(token, uid).toBitLabsHeaders()

        bitLabsAPI.updateClick(headers, clickId, UpdateClickBody(LeaveReason(reason)))
            .enqueue(object : Callback<BitLabsResponse<Unit>> {
                override fun onResponse(
                    call: Call<BitLabsResponse<Unit>>, response: Response<BitLabsResponse<Unit>>,
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
    }


    internal fun getSurveys(
        token: String,
        uid: String,
        sdk: String,
        onResponseListener: OnResponseListener<List<Survey>>,
        onExceptionListener: OnExceptionListener,
    ) {
        val headers = Pair(token, uid).toBitLabsHeaders()

        bitLabsAPI.getSurveys(headers, sdk)
            .enqueue(object : Callback<BitLabsResponse<GetSurveysResponse>> {
                override fun onResponse(
                    call: Call<BitLabsResponse<GetSurveysResponse>>,
                    response: Response<BitLabsResponse<GetSurveysResponse>>,
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

                override fun onFailure(
                    call: Call<BitLabsResponse<GetSurveysResponse>>,
                    t: Throwable,
                ) {
                    SentryManager.captureException(t)
                    onExceptionListener.onException(Exception(t))
                }
            })
    }

    internal fun getAppSettings(
        token: String,
        onResponseListener: OnResponseListener<GetAppSettingsResponse>,
        onExceptionListener: OnExceptionListener,
    ) = bitLabsAPI.getAppSettings(url = "https://dashboard.bitlabs.ai/api/public/v1/apps/$token")
        .enqueue(object : Callback<GetAppSettingsResponse> {
            override fun onResponse(
                call: Call<GetAppSettingsResponse>,
                response: Response<GetAppSettingsResponse>,
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { onResponseListener.onResponse(it) }
                    return
                }
            }

            override fun onFailure(
                call: Call<GetAppSettingsResponse>, t: Throwable,
            ) = with(Exception(t)) {
                SentryManager.captureException(this)
                onExceptionListener.onException(this)
            }
        })
}

private fun Pair<String, String>.toBitLabsHeaders() = mapOf(
    "X-Api-Token" to first,
    "X-User-Id" to second,
)

package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.LeaveReason
import ai.bitlabs.sdk.data.model.body
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.TAG
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(token: String, uid: String) : Serializable {

    init {
        BitLabsAPI.setup(token, uid)
    }

    internal fun hasSurveys(onResponseListener: OnResponseListener<Boolean>) =
        BitLabsAPI().checkSurveys().enqueue(object : Callback<BitLabsResponse> {
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
    ) = BitLabsAPI().leaveSurvey(networkId, surveyId, LeaveReason(reason))
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
}
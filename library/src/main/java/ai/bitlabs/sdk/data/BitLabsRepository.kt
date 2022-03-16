package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.body
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.OnResponseListener
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "BitLabs"

class BitLabsRepository(private val token: String, private val uid: String) {
    fun hasSurveys(onResponseListener: OnResponseListener) =
        BitLabsAPI().checkSurveys(token, uid).enqueue(object : Callback<BitLabsResponse> {
            override fun onResponse(
                call: Call<BitLabsResponse>,
                response: Response<BitLabsResponse>
            ) {
                if (response.isSuccessful) {
                    onResponseListener.onResponse(response.body()?.data?.has_surveys)
                } else {
                    Log.e(TAG, "Response Error - ${response.errorBody()?.body()}")
                    onResponseListener.onResponse(null)
                }
            }

            override fun onFailure(call: Call<BitLabsResponse>, t: Throwable) {
                Log.e(TAG, "Failure - ${t.message ?: "Unknown Error"}")
                onResponseListener.onResponse(null)
            }
        })
}
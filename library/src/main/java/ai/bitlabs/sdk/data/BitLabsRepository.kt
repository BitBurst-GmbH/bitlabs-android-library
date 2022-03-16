package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.network.BitLabsAPI
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BitLabsRepository(val token: String, val uid: String) {
    private val TAG = "BITLABS"
    fun hasSurveys(onResponse: (Boolean) -> Unit) {
        BitLabsAPI().checkSurveys(token, uid).enqueue(object : Callback<BitLabsResponse> {
            override fun onResponse(
                call: Call<BitLabsResponse>,
                response: Response<BitLabsResponse>
            ) {
                if (response.isSuccessful && response.body()?.data != null) {
                    onResponse(response.body()!!.data.has_surveys)
                } else {
                    Log.e(TAG, "Response Error - ${response.body()!!.error.details}")
                    onResponse(false)
                }
            }

            override fun onFailure(call: Call<BitLabsResponse>, t: Throwable) {
                Log.e(TAG, "Failure - ${t.message ?: "Unknown Error"}")
                onResponse(false)
            }

        })
    }
}
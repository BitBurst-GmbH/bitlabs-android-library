package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.model.WebActivityParams
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class BitLabsSDK {
    interface RewardListener<T> {
        fun onReward(payout: Float)
    }

    interface Listener<T> {
        fun onResponse(response: T)
    }

    interface ErrorListener {
        fun onError(error: Error)
    }

    companion object {
        internal val instance = BitLabsSDK()
        var rewardListener: RewardListener<Float>? = null

        fun init(context: Context, token: String, userID: String) {
            if (token.isEmpty() || userID.isEmpty())
                throw RuntimeException("both token and userID have to be non-empty")

            instance.requestQueue?.cancelAll { true }
            instance.requestQueue = Volley.newRequestQueue(context)

            instance.config = WebActivityParams(token, userID)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mgr = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                instance.isTablet = mgr?.phoneType == TelephonyManager.PHONE_TYPE_NONE
            }
        }
    }

    private var config: WebActivityParams? = null
    private var requestQueue: RequestQueue? = null
    private var isTablet: Boolean = false

    private class BitLabsRequest(
        val config: WebActivityParams?,
        url: String?,
        body: JSONObject?,
        listener: Response.Listener<JSONObject>?,
        errorListener: Response.ErrorListener?
    ) : JsonObjectRequest(url, body, listener, errorListener) {
        override fun getHeaders(): MutableMap<String?, String?> {
            return mutableMapOf(
                "X-Api-Token" to config?.token,
                "X-User-Id" to config?.uid,
            )
        }
    }

    private fun VolleyError.toError(): Error {
        return if (networkResponse != null && networkResponse.data != null) {
            val responseData = String(networkResponse.data, Charsets.UTF_8)
            Error("backend error: $responseData")
        } else {
            Error("network error: ${message ?: this}")
        }
    }

    internal fun reportSurveyLeave(networkID: String, surveyID: String, reason: String) {
        val req = BitLabsRequest(
            config,
            "https://api.bitlabs.ai/v1/client/networks/$networkID/surveys/$surveyID/leave",
            JSONObject(mapOf("reason" to reason)),
            {},
            { error -> Log.e("BitLabs", "survey leave send error: ${error.toError()}") }
        )
        requestQueue?.add(req)
    }
}

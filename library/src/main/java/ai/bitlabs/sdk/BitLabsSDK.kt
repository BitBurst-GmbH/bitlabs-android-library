package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.model.WebActivityParams
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.unity3d.player.UnityPlayer
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

        fun hasSurveys(listener: Listener<Boolean>?, errorListener: ErrorListener?) {
            instance.hasSurveyAvailable(listener, errorListener)
        }

        fun hasSurveys(gameObject: String) {
            hasSurveys(
                object: Listener<Boolean> {
                    override fun onResponse(response: Boolean) {
                        UnityPlayer.UnitySendMessage(gameObject, "OnHasSurveys", response.toString())
                    }
                },
                object: ErrorListener {
                    override fun onError(error: Error) {
                        Log.e("BitLabsActivity", error.toString())
                    }
                }
            )
        }

        fun onReward(listener: RewardListener<Float>?) {
            rewardListener = listener
        }

        fun onReward(gameObject: String) {
            onReward(
                object: RewardListener<Float> {
                    override fun onReward(payout: Float) {
                        UnityPlayer.UnitySendMessage(gameObject, "OnReward", payout.toString())
                    }
                }
            )
        }

        fun setTags(tags: Map<String, String>) {
            instance.config?.tags = tags.toMutableMap()
        }

        fun appendTag(key: String, value: String) {
            instance.config?.tags?.put(key, value)
        }

        fun show(context: Context) {
            val intent = Intent(context, WebActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(BUNDLE_KEY_PARAMS, instance.config)
            context.startActivity(intent)
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

    private fun hasSurveyAvailable(listener: Listener<Boolean>?, errorListener: ErrorListener?) {
        val platform = if (isTablet) "TABLET" else "MOBILE"

        val req = BitLabsRequest(
            config,
            "https://api.bitlabs.ai/v1/client/check?platform=$platform",
            null,
            Response.Listener { response: JSONObject ->
                if (response["status"] != "success") {
                    errorListener?.onError(Error("api error: $response"))
                    return@Listener
                }

                try {
                    val result = response.getJSONObject("data").getBoolean("has_surveys")
                    listener?.onResponse(result)
                } catch (e: JSONException) {
                    errorListener?.onError(Error(e))
                }
            },
            { error -> errorListener?.onError(error.toError()) }
        )
        requestQueue?.add(req)
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

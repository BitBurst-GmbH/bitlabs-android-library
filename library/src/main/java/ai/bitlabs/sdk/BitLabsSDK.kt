package ai.bitlabs.sdk


import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*


class BitLabsSDK() {

    private var token: String? = null
    private var userID: String? = null
    private var hasSurveys: Boolean = false

    private var colorDark: String = "#2196F3"
    private var colorLight: String = "#1976D2"
    private var colorAccent: String = "#FF9800"

    private var tags: String = ""

    companion object {
        private val instance = BitLabsSDK()

        var HEADER: MutableMap<String?, String?>? = null

        fun init(context: Context, token: String, userID: String) {
            when {
                token == "" -> Log.e("BitLabs", "Missing Token")
                userID == "" -> Log.e("BitLabs", "Missing User ID")
                else -> {
                    instance.token = token
                    instance.userID = userID
                    instance.hasSurveys = false

                    HEADER =
                            hashMapOf("X-Api-Token" to instance.token, "X-User-Id" to instance.userID)

                    update(context)
                }
            }
        }

        fun surveyAvailable() = instance.hasSurveys

        fun setTags(tags: Map<String, Any>) {
            var query = ""
            for ((k, v) in tags) {
                query += "&" + URLEncoder.encode(k, "utf-8") + "=" + URLEncoder.encode(v.toString(), "utf-8")
            }
            instance.tags = query
        }

        fun show(context: Context) {
            if (instance.hasSurveys) {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.BUNDLE_KEY_TOKEN, instance.token)
                intent.putExtra(WebActivity.BUNDLE_KEY_USER_ID, instance.userID)
                intent.putExtra(WebActivity.BUNDLE_KEY_COLOR_DARK, instance.colorDark)
                intent.putExtra(WebActivity.BUNDLE_KEY_COLOR_LIGHT, instance.colorLight)
                intent.putExtra(WebActivity.BUNDLE_KEY_COLOR_ACCENT, instance.colorAccent)
                intent.putExtra(WebActivity.BUNDLE_KEY_TAGS, instance.tags)
                context.startActivity(intent)
            }
        }

        private fun update(context: Context) {
            val requestQueue = Volley.newRequestQueue(context)

            // Check for available Surveys
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val isTablet: Boolean

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            isTablet =
                    Objects.requireNonNull(manager).phoneType == TelephonyManager.PHONE_TYPE_NONE;

            val checkRequest = object : StringRequest(
                    Method.GET,
                    "https://api.bitlabs.ai/v1/client/check?platform=" + if (isTablet) "TABLET" else "MOBILE",
                    Response.Listener { response: String ->
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getString("status") == "success") {
                            instance.hasSurveys =
                                    jsonObject.getJSONObject("data").getBoolean("has_surveys")
                        } else instance.hasSurveys = false
                    },
                    Response.ErrorListener { error ->
                        val charset: Charset = Charsets.UTF_8
                        if (error?.networkResponse != null)
                            Log.e("BitLabs", String(error.networkResponse.data, charset))
                    }
            ) {
                override fun getHeaders(): MutableMap<String?, String?>? {
                    return HEADER
                }
            }

            // Get App Settings
            val settingsRequest = object : StringRequest(
                    Method.GET,
                    "https://api.bitlabs.ai/v1/client/settings",
                    Response.Listener { response: String ->
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getString("status") == "success") {
                            val visual = jsonObject.getJSONObject("data").getJSONObject("visual")
                            instance.colorDark = visual.getString("color_dark")
                            instance.colorLight = visual.getString("color_light")
                            instance.colorAccent = visual.getString("color_accent")
                        }
                    },
                    Response.ErrorListener { error ->
                        val charset: Charset = Charsets.UTF_8
                        if (error?.networkResponse != null)
                            Log.e("BitLabs", String(error.networkResponse.data, charset))
                    }
            ) {
                override fun getHeaders(): MutableMap<String?, String?>? {
                    return HEADER
                }
            }

            requestQueue.add(checkRequest)
            requestQueue.add(settingsRequest)
        }
    }
}

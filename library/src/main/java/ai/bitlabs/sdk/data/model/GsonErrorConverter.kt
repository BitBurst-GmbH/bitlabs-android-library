package ai.bitlabs.sdk.data.model

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody

fun ResponseBody.body(): BitLabsResponse? =
    try {
        GsonBuilder().create().fromJson(this.string(), BitLabsResponse::class.java)
    } catch (e: Exception) {
        null
    }
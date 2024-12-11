package ai.bitlabs.sdk.util

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type


/**
 * Returns a [HookMessage] object converted from JSON.
 * @receiver The post message data string that received from the webview
 */
internal fun String.asHookMessage(): HookMessage<*>? = try {
    GsonBuilder()
        .registerTypeAdapter(HookMessage::class.java, HookMessageDeserializer())
        .create()
        .fromJson(this, HookMessage::class.java)
} catch (e: Exception) {
    Log.e(TAG, e.toString())
    null
}

/**
 * class to deserialize the message received from the webview.
 */
internal class HookMessageDeserializer : JsonDeserializer<HookMessage<*>> {
    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): HookMessage<*>? {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")

        jsonObject.isHookMessage() || return null

        val type = jsonObject.get("type").asString
        val name = context?.deserialize<HookName>(jsonObject.get("name"), HookName::class.java)
            ?: throw JsonParseException("Invalid name: ${jsonObject.get("name")}.")

        val argsJsonArray = jsonObject.getAsJsonArray("args")

        val args: List<Any> = when (name) {
            HookName.INIT -> argsJsonArray.map {
                context.deserialize<Unit>(it, Unit::class.java)
            }

            HookName.SDK_CLOSE -> argsJsonArray.map {
                context.deserialize<Unit>(it, Unit::class.java)
            }

            HookName.SURVEY_START -> argsJsonArray.map {
                context.deserialize<SurveyStartArgs>(it, SurveyStartArgs::class.java)
            }

            HookName.SURVEY_COMPLETE -> argsJsonArray.map {
                context.deserialize<RewardArgs>(it, RewardArgs::class.java)
            }

            HookName.SURVEY_SCREENOUT -> argsJsonArray.map {
                context.deserialize<RewardArgs>(it, RewardArgs::class.java)
            }

            HookName.SURVEY_START_BONUS -> argsJsonArray.map {
                context.deserialize<RewardArgs>(it, RewardArgs::class.java)
            }
        }

        return HookMessage(type = type, name = name, args = args)
    }
}

/**
 * Check if JSON is of shape HookMessage
 */
internal fun JsonElement.isHookMessage(): Boolean {
    val jsonObject = this.asJsonObject
    return jsonObject.has("type") && jsonObject.has("name") && jsonObject.has("args")
}

/**
 * HookMessage data class that holds the message received from the webview.
 */
@Keep
internal data class HookMessage<T>(
    val type: String, val name: HookName, val args: List<T>
)

/**
 * HookName enum class that holds the name of the hook message.
 */
internal enum class HookName {
    @SerializedName("offerwall-core:init")
    INIT,

    @SerializedName("offerwall-core:sdk.close")
    SDK_CLOSE,

    @SerializedName("offerwall-surveys:survey.start")
    SURVEY_START,

    @SerializedName("offerwall-surveys:survey.complete")
    SURVEY_COMPLETE,

    @SerializedName("offerwall-surveys:survey.screenout")
    SURVEY_SCREENOUT,

    @SerializedName("offerwall-surveys:survey.start-bonus")
    SURVEY_START_BONUS,
}

/**
 * Reward Arguments data class that holds the reward data.
 */
@Keep
internal data class RewardArgs(
    val reward: Float
)

/**
 * Survey Start Arguments data class that holds the survey start data.
 */
@Keep
internal data class SurveyStartArgs(
    val clickId: String, val link: String
)
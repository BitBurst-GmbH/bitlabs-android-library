package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.OnRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.views.WebActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.GsonBuilder
import com.unity3d.player.UnityPlayer

/**
 * The main class including all the library functions to use in your code.
 * ######
 * This is a singleton object, so you'll have one instance throughout the whole
 * main process(app lifecycle).
 */
object BitLabs {
    private var uid: String = ""
    private var adId: String = ""
    private var token: String = ""

    /** These will be added as query parameters to the OfferWall Link */
    var tags: MutableMap<String, Any> = mutableMapOf()

    private var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnRewardListener? = null

    /**
     * Initialises the connection with BitLabs API using your app [token] and [uid]
     * and gets the user [Advertising Id][AdvertisingIdClient.Info] using the activity [context].
     * ######
     * **IMPORTANT:** This is the essential function. Without it, the library will not function
     * properly. So make sure you call it before using the library's functions.
     * @param[token] Found on your [BitLabs Dashboard](https://dashboard.bitlabs.ai/),
     * @param[uid] Unique for every user to initialise the connection with the BitLabs API.
     */
    fun init(context: Context, token: String, uid: String) {
        this.token = token
        this.uid = uid
        bitLabsRepo = BitLabsRepository(token, uid)
        determineAdvertisingInfo(context)
    }

    /** Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the checkSurveysCallback().
     * ######
     * If you want to perform background checks if surveys are available, this is the best option.
     */
    fun checkSurveys(gameObject: String) = ifInitialised {
        bitLabsRepo?.checkSurveys({ hasSurveys ->
            UnityPlayer.UnitySendMessage(gameObject, "CheckSurveysCallback", hasSurveys.toString())
        }, { exception ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "CheckSurveysException",
                exception.message.toString()
            )
        })
    }

    /**
     * Fetches a list of surveys the user can open.
     * ######
     * If the user still has to answer a qualification before more surveys can be returned,
     * then this will return 3 random Surveys just for display.
     * ######
     * The getSurveysCallback() is executed when a response is received.
     * Its parameter is the String in format of JSON list of surveys in . If it's `null`,
     * then there has been an internal error which is most probably logged with 'BitLabs' as a tag.
     */
    fun getSurveys(gameObject: String) = ifInitialised {
        bitLabsRepo?.getSurveys("UNITY", { surveys ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "GetSurveysCallback",
                GsonBuilder().create().toJson(surveys)
            )
        }, { exception ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "GetSurveysException",
                exception.message.toString()
            )
        })
    }

    /** Registers an [OnRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    fun setOnRewardListener(gameObject: String) {
        onRewardListener = OnRewardListener { payout ->
            UnityPlayer.UnitySendMessage(gameObject, "rewardCallback", payout.toString())
        }
    }

    /** Adds a new tag([key]:[value] pair) to [BitLabs.tags] */
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the [context] of the Activity you pass.
     * ######
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    fun launchOfferWall(context: Context) = ifInitialised {
        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_PARAMS, WebActivityParams(token, uid, "UNITY", adId, tags).url)
            context.startActivity(this)
        }
    }

    internal fun leaveSurvey(networkId: String, surveyId: String, reason: String) =
        bitLabsRepo?.leaveSurvey(networkId, surveyId, reason)

    private fun determineAdvertisingInfo(context: Context) = Thread {
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            Log.d(TAG, "Advertising Id: $adId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to determine Advertising Id", e)
        }
    }.start()

    /**
     * Checks whether [token] and [uid] have been set and aren't blank/empty and
     * [bitLabsRepo] is initialised and executes the [block] accordingly.
     */
    private inline fun ifInitialised(block: () -> Unit) {
        val isInitialised = token.isNotBlank().and(uid.isNotBlank()).and(bitLabsRepo != null)

        if (isInitialised) block()
        else Log.e(TAG, "You should initialise BitLabs first! Call BitLabs::init()")
    }
}
package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnRewardListener
import ai.bitlabs.sdk.util.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import com.unity3d.player.UnityPlayer

/**
 * The main class including all the library functions to use in your code.
 *
 * This is a singleton object, so you'll have one instance throughout the whole
 * main process(app lifecycle).
 */
object BitLabs {
    private var token: String = ""
    private var uid: String = ""

    /** These will be added as query parameters to the OfferWall Link */
    var tags: MutableMap<String, Any> = mutableMapOf()

    private var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnRewardListener? = null


    /**
     * This is the essential function. Without it, the library will not function properly.
     * So make sure you call it before using the library's functions
     * @param token Your App Token, found in your [BitLabs Dashboard](https://dashboard.bitlabs.ai/).
     * @param uid The id of the current user, this id is for you to keep track of which user got what.
     */
    fun init(token: String, uid: String) {
        this.token = token
        this.uid = uid
        bitLabsRepo = BitLabsRepository(token, uid)
    }

    /** Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the [OnResponseListener.onResponse].
     *
     * If you want to perform background checks if surveys are available, this is the best option.
     *
     * @param[onResponseListener] The callback to execute when a response is received, the boolean
     * parameter is `true` if an action can be performed and `false` otherwise. If it's `null`,
     * then there has been an internal error which is most probably logged with 'BitLabs' as a tag.
     */
    fun checkSurveys(onResponseListener: OnResponseListener<Boolean>) = ifInitialised {
        bitLabsRepo?.checkSurveys(onResponseListener)
    }

    /** Don't use this method, use [BitLabs.checkSurveys] instead. */
    fun checkSurveys(gameObject: String) = checkSurveys { hasSurveys ->
        if (hasSurveys != null)
            UnityPlayer.UnitySendMessage(
                gameObject,
                "BitLabs - checkSurveys",
                if (hasSurveys) "Found Surveys!" else "No Surveys!"
            )
    }

    /** Registers an [OnRewardListener] callback to be invoked when a Survey is completed by the user. */
    fun setOnRewardListener(onRewardListener: OnRewardListener) {
        this.onRewardListener = onRewardListener
    }

    /** Don't use this method, use [BitLabs.setOnRewardListener] instead */
    fun setOnRewardListener(gameObject: String) = setOnRewardListener { payout ->
        UnityPlayer.UnitySendMessage(gameObject, "BitLabs - OnReward", payout.toString())
    }

    /** Adds a new tag([key]:[value] pair) to [BitLabs.tags] */
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the [context] of the Activity you pass.
     *
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    fun launchOfferWall(context: Context) = ifInitialised {
        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_PARAMS, WebActivityParams(token, uid, "UNITY", tags).url)
            context.startActivity(this)
        }
    }

    internal fun leaveSurvey(networkId: String, surveyId: String, reason: String) =
        bitLabsRepo?.leaveSurvey(networkId, surveyId, reason) { }

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
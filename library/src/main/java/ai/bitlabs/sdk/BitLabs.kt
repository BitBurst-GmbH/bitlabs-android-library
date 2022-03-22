package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.LeaveSurveyListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnRewardListener
import android.content.Context
import android.content.Intent
import com.unity3d.player.UnityPlayer
import java.io.Serializable

/**
 * The main class including all the library functions to use in your code.
 * @param token Your App Token, found in your [BitLabs Dashboard](https://dashboard.bitlabs.ai/).
 * @param uid The id of the current user, this id is for you to keep track of which user got what.
 */
class BitLabs(private val token: String, private val uid: String) : Serializable {
    /** These will be added as query parameters to the OfferWall Link */
    var tags: MutableMap<String, Any> = mutableMapOf()
    private val leaveSurveyListener: LeaveSurveyListener
    private var onRewardListener: OnRewardListener? = null
    private val bitLabsRepo = BitLabsRepository(token, uid)

    init {
        leaveSurveyListener = LeaveSurveyListener { networkId, surveyId, reason, payout ->
            bitLabsRepo.leaveSurvey(networkId, surveyId, reason) {
                onRewardListener?.onReward(payout)
            }
        }
    }

    /** Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the [OnResponseListener.onResponse].
     *
     * If you want to perform background checks if surveys are available, this is the best option.
     *
     * @param[onResponseListener] The callback to execute when a response is received, the boolean
     * parameter is `true` if an action can be performed and `false` otherwise.
     */
    fun hasSurveys(onResponseListener: OnResponseListener) =
        bitLabsRepo.hasSurveys(onResponseListener)

    /** Don't use this method, use [BitLabs.hasSurveys] instead. */
    fun hasSurveys(gameObject: String) = hasSurveys { hasSurveys ->
        if (hasSurveys != null)
            UnityPlayer.UnitySendMessage(
                gameObject,
                "BitLabs - hasSurveys",
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
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    @JvmOverloads
    fun launchOfferWall(context: Context, sdk: String = "NATIVE") =
        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_PARAMS,
                WebActivityParams(token, uid, sdk, tags, leaveSurveyListener)
            )
            context.startActivity(this)
        }
}
package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnRewardListener
import android.content.Context
import android.content.Intent
import com.unity3d.player.UnityPlayer

class BitLabs(private val token: String, private val uid: String) {
    var tags: MutableMap<String, Any> = mutableMapOf()
    private var onRewardListener: OnRewardListener? = null
    private val bitLabsRepo = BitLabsRepository(token, uid)

    fun hasSurveys(onResponseListener: OnResponseListener) =
        bitLabsRepo.hasSurveys(onResponseListener)

    fun hasSurveys(gameObject: String) = hasSurveys { hasSurveys ->
        if (hasSurveys != null)
            UnityPlayer.UnitySendMessage(
                gameObject,
                "BitLabs - hasSurveys",
                if (hasSurveys) "Found Surveys!" else "No Surveys!"
            )
    }

    fun setOnReward(onRewardListener: OnRewardListener) {
        this.onRewardListener = onRewardListener
    }

    fun setOnReward(gameObject: String) = setOnReward { payout ->
        UnityPlayer.UnitySendMessage(gameObject, "BitLabs - OnReward", payout.toString())
    }

    fun appendTag(key: String, value: Any) {
        tags[key] = value
    }

    fun launchOfferWall(context: Context) =
        context.startActivity(Intent(context, WebActivity::class.java).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_PARAMS, WebActivityParams(token, uid))
        })
}
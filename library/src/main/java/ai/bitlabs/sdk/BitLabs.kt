package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import android.content.Context
import android.content.Intent

class BitLabs(private val token: String, private val uid: String) {
    var tags: MutableMap<String, Any> = mutableMapOf()
    private val bitlabsRepo = BitLabsRepository(token, uid)

    fun hasSurveys(onResponse: (Boolean) -> Unit) = bitlabsRepo.hasSurveys(onResponse)

    fun appendTag(key: String, value: Any) {
        tags[key] = value
    }

    fun launchOfferWall(context: Context) {
        context.startActivity(Intent(context, WebActivity::class.java).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BUNDLE_KEY_PARAMS, WebActivityParams(token, uid))
        })
    }

}
package ai.bitlabs.sdk.offerwall.util

import androidx.annotation.Keep
import androidx.core.net.toUri

/**
 * This class holds the parameters of the [ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity] responsible to launch the OfferWall.
 * @constructor A Constructor that holds the values ([token], [uid], [tags])
 * which will be used in the [ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity] to launch the OfferWall correctly.
 */
@Keep
internal data class OfferwallUrl(
    private val token: String,
    private val uid: String,
    private val sdk: String,
    private val maid: String,
    private val tags: Map<String, Any> = mapOf(),
) {

    val url: String by lazy { baseUri().build().toString() }

    fun offerUrl(offerId: String) = baseUri().appendPath("offers")
        .appendQueryParameter("offer-id", offerId)
        .build().toString()

    fun magicReceiptsOfferUrl(offerId: String) = baseUri()
        .appendPath("magic-receipts")
        .appendPath("offer")
        .appendPath(offerId)
        .build().toString()

    fun magicReceiptsMerchantUrl(merchantId: String) = baseUri()
        .appendPath("magic-receipts")
        .appendPath("merchant")
        .appendPath(merchantId)
        .build().toString()

    private fun baseUri() = "https://web.bitlabs.ai".toUri().buildUpon()
        .appendQueryParameter("os", "ANDROID")
        .appendQueryParameter("token", token)
        .appendQueryParameter("uid", uid)
        .appendQueryParameter("sdk", sdk)
        .apply { if (maid.isNotEmpty()) appendQueryParameter("maid", maid) }
        .apply { tags.forEach { tag -> appendQueryParameter(tag.key, tag.value.toString()) } }
}